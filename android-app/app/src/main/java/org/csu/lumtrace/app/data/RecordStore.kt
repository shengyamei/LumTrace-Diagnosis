package org.csu.lumtrace.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.recordDataStore by preferencesDataStore(name = "lumtrace_records")

/**
 * 一条本地病程记录：每次导诊评估后自动写入，保存在本机。
 */
data class LocalRecord(
    val id: Long = 0L,
    val time: Long = 0L,
    val source: String = "在线",
    val topDisease: String = "",
    val confidence: Double = 0.0,
    val severity: Int = 1,
    val suggestion: String? = null,
    val symptomNames: List<String> = emptyList(),
    val candidates: List<String> = emptyList()
)

object RecordStore {

    private const val MAX_RECORDS = 50

    private val key = stringPreferencesKey("records_json")
    private val gson = Gson()
    private val listType = object : TypeToken<List<LocalRecord>>() {}.type

    suspend fun load(context: Context): List<LocalRecord> {
        val json = context.recordDataStore.data.map { it[key] }.first()
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            gson.fromJson<List<LocalRecord>>(json, listType)
        }.getOrNull() ?: emptyList()
    }

    suspend fun add(context: Context, record: LocalRecord) {
        val next = (listOf(record) + load(context)).take(MAX_RECORDS)
        context.recordDataStore.edit { it[key] = gson.toJson(next) }
    }

    suspend fun remove(context: Context, id: Long) {
        val next = load(context).filterNot { it.id == id }
        context.recordDataStore.edit { it[key] = gson.toJson(next) }
    }

    suspend fun clear(context: Context) {
        context.recordDataStore.edit { it.remove(key) }
    }
}