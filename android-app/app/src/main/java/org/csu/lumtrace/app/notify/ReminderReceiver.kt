package org.csu.lumtrace.app.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 闹钟触发后发本地通知，并为下一天同一时间点重新排闹钟。
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra("reminderId", -1L)
        val index = intent.getIntExtra("timeIndex", 0)
        val time = intent.getStringExtra("time") ?: ""
        val name = intent.getStringExtra("name") ?: "用药提醒"
        val dosage = intent.getStringExtra("dosage") ?: ""

        ReminderNotifier.show(context, id, name, dosage, time)

        if (id > 0 && time.isNotBlank()) {
            ReminderScheduler.scheduleOne(context, id, index, time, name, dosage)
        }
    }
}