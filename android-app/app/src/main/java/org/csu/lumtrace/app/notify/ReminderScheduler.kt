package org.csu.lumtrace.app.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import org.csu.lumtrace.app.data.MedicineReminder
import java.util.Calendar

/**
 * 用药提醒的闹钟调度：每条提醒的每个时间点各注册一个 AlarmManager 闹钟。
 */
object ReminderScheduler {

    fun scheduleAll(context: Context, reminder: MedicineReminder) {
        cancelAll(context, reminder)
        if (!reminder.enabled) return
        reminder.times.forEachIndexed { index, time ->
            scheduleOne(context, reminder.id, index, time, reminder.name, reminder.dosage)
        }
    }

    fun scheduleOne(context: Context, id: Long, index: Int, time: String, name: String, dosage: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = nextTriggerAt(time)
        val pi = pendingIntent(context, id, index, time, name, dosage)
        // 精确闹钟（SCHEDULE_EXACT_ALARM）在 Android 14+ 属于特殊权限，默认不授予；
        // 未授权时任何精确接口都会抛 SecurityException，必须降级为不精确闹钟，否则会崩溃。
        val exact = canExact(am) && runCatching {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }.isSuccess
        if (!exact) {
            runCatching { am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi) }
        }
    }

    fun cancelAll(context: Context, reminder: MedicineReminder) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        reminder.times.forEachIndexed { index, time ->
            am.cancel(pendingIntent(context, reminder.id, index, time, reminder.name, reminder.dosage))
        }
    }

    fun nextTriggerAt(time: String): Long {
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis() + 1000L) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    private fun canExact(am: AlarmManager): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()

    private fun pendingIntent(
        context: Context,
        id: Long,
        index: Int,
        time: String,
        name: String,
        dosage: String
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminderId", id)
            putExtra("timeIndex", index)
            putExtra("time", time)
            putExtra("name", name)
            putExtra("dosage", dosage)
        }
        val requestCode = (id % 100000L).toInt() * 100 + index
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}