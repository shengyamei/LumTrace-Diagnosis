package org.csu.lumtrace.app.ui.result

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.csu.lumtrace.app.data.CandidateDto
import org.csu.lumtrace.app.data.DiagnosisHolder
import org.csu.lumtrace.app.data.DiagnosisResponse
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(onBack: () -> Unit) {
    val result = DiagnosisHolder.last

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("评估结果") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("返回") }
                }
            )
        }
    ) { padding ->
        if (result == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("结果已失效，请重新评估", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onBack) { Text("重新评估") }
            }
        } else {
            ResultContent(
                result = result,
                onBack = onBack,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun ResultContent(
    result: DiagnosisResponse,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (DiagnosisHolder.isOffline) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "当前为离线演示结果：服务器不可用，已按内置规则给出参考方向，未上传任何数据。",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "以下内容仅为健康问题参考方向，不构成诊断结论，不能替代执业医师。如出现胸痛、呼吸困难、意识模糊等紧急情况，请立即拨打 120。",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "最可能相关",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        result.topDisease ?: "暂无明显匹配",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    Row {
                        Text("参考置信度：", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${(result.confidence * 100).roundToInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Row {
                        Text("关注程度：", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            severityText(result.severity),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = severityColor(result.severity)
                        )
                    }
                }
            }
        }

        val suggestion = result.suggestion
        if (!suggestion.isNullOrBlank()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("建议", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(suggestion, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        val others = result.candidates.filter { it.disease != result.topDisease }
        if (others.isNotEmpty()) {
            item {
                Text("其他可能方向", style = MaterialTheme.typography.titleMedium)
            }
            items(others) { c ->
                CandidateRow(c)
            }
        }

        item {
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("重新评估")
            }
        }
    }
}

@Composable
private fun CandidateRow(c: CandidateDto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    c.disease,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${(c.score * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (c.hitSymptoms.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "匹配症状：" + c.hitSymptoms.joinToString("、"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
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