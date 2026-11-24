package org.csu.lumtrace.app.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.csu.lumtrace.app.data.ReminderStore

/**
 * 开机或应用升级后恢复所有启用中的用药提醒闹钟。
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ReminderStore.load(context)
                    .filter { it.enabled && it.times.isNotEmpty() }
                    .forEach { ReminderScheduler.scheduleAll(context, it) }
            } finally {
                pending.finish()
            }
        }
    }
}