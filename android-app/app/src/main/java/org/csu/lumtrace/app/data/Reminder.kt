package org.csu.lumtrace.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.reminderDataStore by preferencesDataStore(name = "lumtrace_reminders")

/**
 * 一条用药提醒：药名 + 剂量 + 每天的时间点（HH:mm）。
 */
data class MedicineReminder(
    val id: Long = 0L,
    val name: String = "",
    val dosage: String = "",
    val times: List<String> = emptyList(),
    val enabled: Boolean = true
)

object ReminderStore {

    val PRESET_TIMES = listOf("08:00", "12:30", "18:30", "21:30")

    private val listKey = stringPreferencesKey("reminders_json")
    private val checkinKey = stringPreferencesKey("checkins_json")
    private val gson = Gson()
    private val listType = object : TypeToken<List<MedicineReminder>>() {}.type
    private val setType = object : TypeToken<Set<String>>() {}.type

    suspend fun load(context: Context): List<MedicineReminder> {
        val json = context.reminderDataStore.data.map { it[listKey] }.first()
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            gson.fromJson<List<MedicineReminder>>(json, listType)
        }.getOrNull() ?: emptyList()
    }

    suspend fun save(context: Context, list: List<MedicineReminder>) {
        context.reminderDataStore.edit { it[listKey] = gson.toJson(list) }
    }

    suspend fun upsert(context: Context, reminder: MedicineReminder) {
        val current = load(context)
        val next = if (current.any { it.id == reminder.id }) {
            current.map { if (it.id == reminder.id) reminder else it }
        } else {
            current + reminder
        }
        save(context, next)
    }

    suspend fun remove(context: Context, id: Long) {
        save(context, load(context).filterNot { it.id == id })
    }

    suspend fun loadCheckins(context: Context): Set<String> {
        val json = context.reminderDataStore.data.map { it[checkinKey] }.first()
        if (json.isNullOrBlank()) return emptySet()
        return runCatching {
            gson.fromJson<Set<String>>(json, setType)
        }.getOrNull() ?: emptySet()
    }

    suspend fun addCheckin(context: Context, key: String) {
        val next = loadCheckins(context) + key
        context.reminderDataStore.edit { it[checkinKey] = gson.toJson(next) }
    }

    fun checkinKey(reminderId: Long, time: String, day: String): String =
        "$reminderId|$time|$day"
}