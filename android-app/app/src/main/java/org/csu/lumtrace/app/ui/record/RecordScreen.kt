package org.csu.lumtrace.app.ui.record

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.csu.lumtrace.app.data.ApiClient
import org.csu.lumtrace.app.data.LocalRecord
import org.csu.lumtrace.app.data.RecordStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class RecordUiState(
    val loading: Boolean = true,
    val records: List<LocalRecord> = emptyList(),
    val detail: LocalRecord? = null
)

class RecordViewModel : ViewModel() {

    var uiState by mutableStateOf(RecordUiState())
        private set

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val list = RecordStore.load(ApiClient.appContext)
            uiState = uiState.copy(loading = false, records = list)
        }
    }

    fun showDetail(record: LocalRecord) {
        uiState = uiState.copy(detail = record)
    }

    fun dismissDetail() {
        uiState = uiState.copy(detail = null)
    }

    fun remove(id: Long) {
        viewModelScope.launch {
            RecordStore.remove(ApiClient.appContext, id)
            val list = RecordStore.load(ApiClient.appContext)
            uiState = uiState.copy(detail = null, records = list)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            RecordStore.clear(ApiClient.appContext)
            uiState = uiState.copy(records = emptyList())
        }
    }
}

private fun formatTime(time: Long): String {
    if (time <= 0L) return "未知时间"
    return SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(Date(time))
}

private fun severityText(severity: Int): String = when {
    severity >= 3 -> "高，建议尽快就医"
    severity == 2 -> "中，建议近期就诊"
    else -> "低，可先观察变化"
}

@Composable
private fun severityColor(severity: Int): Color = when {
    severity >= 3 -> MaterialTheme.colorScheme.error
    severity == 2 -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.primary
}

private fun buildExportText(records: List<LocalRecord>): String {
    val sb = StringBuilder("溯光智诊 · 病程记录\n\n")
    records.forEach { r ->
        sb.append("时间：").append(formatTime(r.time)).append('\n')
        sb.append("参考方向：").append(r.topDisease).append('\n')
        sb.append("关注程度：").append(severityText(r.severity)).append('\n')
        if (r.symptomNames.isNotEmpty()) {
            sb.append("症状：").append(r.symptomNames.joinToString("、")).append('\n')
        }
        val suggestion = r.suggestion
        if (!suggestion.isNullOrBlank()) {
            sb.append("建议：").append(suggestion).append('\n')
        }
        sb.append("----------------\n")
    }
    sb.append("\n本记录仅供参考，不构成诊断，不能替代执业医师。")
    return sb.toString()
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    onBack: () -> Unit,
    viewModel: RecordViewModel = viewModel()
) {
    val state = viewModel.uiState
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("病程记录") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("返回") }
                }
            )
        },
        bottomBar = {
            if (state.records.isNotEmpty()) {
                Surface(shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, buildExportText(state.records))
                                }
                                context.startActivity(
                                    Intent.createChooser(intent, "导出病程记录")
                                )
                            },
                            modifier = Modifier.weight(1f).height(52.dp)
                        ) {
                            Text("导出文本")
                        }
                        OutlinedButton(
                            onClick = viewModel::clearAll,
                            modifier = Modifier.height(52.dp)
                        ) {
                            Text("清空")
                        }
                    }
                }
            }
        }
    ) { padding ->
        when {
            state.loading -> Unit

            state.records.isEmpty() -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("还没有病程记录", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    "在「AI 导诊分诊」完成一次评估后，会自动记录在本机",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "记录保存在本机，最多保留 50 条；点击可查看详情。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(state.records, key = { it.id }) { record ->
                    RecordCard(record) { viewModel.showDetail(record) }
                }
            }
        }

        state.detail?.let { record ->
            AlertDialog(
                onDismissRequest = viewModel::dismissDetail,
                title = { Text("记录详情") },
                text = {
                    Column {
                        Text("时间：" + formatTime(record.time))
                        Text("参考方向：" + record.topDisease)
                        Text("参考置信度：" + (record.confidence * 100).roundToInt() + "%")
                        Text(
                            "关注程度：" + severityText(record.severity),
                            color = severityColor(record.severity),
                            fontWeight = FontWeight.Bold
                        )
                        if (record.symptomNames.isNotEmpty()) {
                            Text("症状：" + record.symptomNames.joinToString("、"))
                        }
                        val suggestion = record.suggestion
                        if (!suggestion.isNullOrBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text("建议：" + suggestion)
                        }
                        val others = record.candidates.filter { it != record.topDisease }
                        if (others.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text("其他方向：" + others.joinToString("、"))
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "本记录仅供参考，不构成诊断。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.remove(record.id) }) {
                        Text("删除此条")
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::dismissDetail) { Text("关闭") }
                }
            )
        }
    }
}

@Composable
private fun RecordCard(record: LocalRecord, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatTime(record.time),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    if (record.source == "离线") "离线" else "在线",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                record.topDisease,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "关注程度：" + severityText(record.severity),
                style = MaterialTheme.typography.bodyMedium,
                color = severityColor(record.severity)
            )
            if (record.symptomNames.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "症状：" + record.symptomNames.joinToString("、"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}