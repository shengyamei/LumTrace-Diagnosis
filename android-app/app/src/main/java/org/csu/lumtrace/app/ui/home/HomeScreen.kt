package org.csu.lumtrace.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.csu.lumtrace.app.data.ApiClient

private data class HomeEntry(val title: String, val desc: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenSymptom: () -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val entries = listOf(
        HomeEntry("AI 导诊分诊", "描述症状，推荐就诊科室"),
        HomeEntry("病程记录", "记录病情变化，可导出文本"),
        HomeEntry("用药提醒", "医嘱录入 + 定时提醒打卡"),
        HomeEntry("用药禁忌", "药品与饮食禁忌查询")
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text("溯光智诊") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        "本应用为就医便民辅助工具，不做疾病诊断、不开处方，不能替代执业医师。如有紧急情况请立即拨打 120。",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            itemsIndexed(entries) { index, entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (index == 0) onOpenSymptom() }
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(entry.title, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            entry.desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            ApiClient.tokenStore.clear()
                            onLogout()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("退出登录")
                }
            }
        }
    }
}