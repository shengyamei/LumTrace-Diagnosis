package org.csu.lumtrace.app.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import org.csu.lumtrace.app.MainActivity

/**
 * 用药提醒的本地通知构建与发送。
 */
object ReminderNotifier {

    const val CHANNEL_ID = "medicine_reminder"

    fun show(context: Context, id: Long, name: String, dosage: String, time: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "用药提醒", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "按医嘱时间提醒服药"
            }
        )

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = if (dosage.isBlank()) "$time 该服药了" else "$time 该服药了：$dosage"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("该吃药了：" + name)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text + "\n点击打开溯光智诊，记录本次服药。"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify((id % 100000L).toInt(), notification)
        } catch (e: SecurityException) {
            // 通知权限被撤销时忽略
        }
    }
}