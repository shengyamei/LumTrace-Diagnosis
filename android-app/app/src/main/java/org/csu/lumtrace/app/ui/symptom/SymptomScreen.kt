package org.csu.lumtrace.app.ui.symptom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.csu.lumtrace.app.data.ApiClient
import org.csu.lumtrace.app.data.ApiException
import org.csu.lumtrace.app.data.DiagnosisHolder
import org.csu.lumtrace.app.data.DiagnosisRequest
import org.csu.lumtrace.app.data.LocalSymptoms
import org.csu.lumtrace.app.data.OfflineTriage
import org.csu.lumtrace.app.data.SymptomDto

data class SymptomUiState(
    val loading: Boolean = true,
    val loadError: String? = null,
    val all: List<SymptomDto> = emptyList(),
    val selected: Set<Long> = emptySet(),
    val category: String? = null,
    val keyword: String = "",
    val submitting: Boolean = false,
    val submitError: String? = null,
    val offline: Boolean = false
) {
    val categories: List<String>
        get() = all.mapNotNull { it.category }.distinct().sorted()

    val filtered: List<SymptomDto>
        get() = all.filter { s ->
            val matchCategory = category == null || s.category == category
            val matchKeyword = keyword.isBlank() ||
                s.name.contains(keyword) ||
                (s.description?.contains(keyword) == true)
            matchCategory && matchKeyword
        }
}

class SymptomViewModel : ViewModel() {

    var uiState by mutableStateOf(SymptomUiState())
        private set

    init {
        load()
    }

    fun load() {
        // 先用内置症状库铺底，避免首屏空等；接口可用时再替换为服务器数据
        uiState = uiState.copy(
            loading = false,
            all = LocalSymptoms.list,
            offline = true,
            loadError = null
        )
        viewModelScope.launch {
            try {
                val result = ApiClient.service().symptoms()
                if (!result.isSuccess) return@launch
                val remote = result.requireData()
                if (remote.isNotEmpty()) {
                    uiState = uiState.copy(all = remote, offline = false)
                }
            } catch (e: Exception) {
                // 保持内置症状库
            }
        }
    }

    fun toggle(id: Long) {
        val next = if (id in uiState.selected) uiState.selected - id else uiState.selected + id
        uiState = uiState.copy(selected = next)
    }

    fun setCategory(value: String?) {
        uiState = uiState.copy(category = value)
    }

    fun setKeyword(value: String) {
        uiState = uiState.copy(keyword = value)
    }

    fun submit(onSuccess: () -> Unit) {
        if (uiState.submitting) return
        if (uiState.selected.isEmpty()) {
            uiState = uiState.copy(submitError = "请至少选择一个症状")
            return
        }
        if (uiState.offline) {
            // 已知离线：直接本地评估，避免等待网络超时
            val picked = uiState.all.filter { it.id in uiState.selected }
            DiagnosisHolder.last = OfflineTriage.evaluate(picked)
            DiagnosisHolder.isOffline = true
            onSuccess()
            return
        }
        viewModelScope.launch {
            uiState = uiState.copy(submitting = true, submitError = null)
            try {
                val result = ApiClient.service().submitDiagnosis(
                    DiagnosisRequest(symptomIds = uiState.selected.toList())
                )
                if (!result.isSuccess) throw ApiException(result.code, result.message ?: "评估失败")
                DiagnosisHolder.last = result.requireData()
                DiagnosisHolder.isOffline = false
                uiState = uiState.copy(submitting = false)
                onSuccess()
            } catch (e: Exception) {
                // 离线降级：接口不可用时按内置规则给出建议就诊方向
                val picked = uiState.all.filter { it.id in uiState.selected }
                DiagnosisHolder.last = OfflineTriage.evaluate(picked)
                DiagnosisHolder.isOffline = true
                uiState = uiState.copy(submitting = false, submitError = null)
                onSuccess()
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomScreen(
    onBack: () -> Unit,
    onSubmitted: () -> Unit,
    viewModel: SymptomViewModel = viewModel()
) {
    val state = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 导诊分诊") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("返回") }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    val err = state.submitError
                    if (err != null) {
                        Text(
                            err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "已选 ${state.selected.size} 项",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = { viewModel.submit(onSubmitted) },
                            enabled = state.selected.isNotEmpty() && !state.submitting,
                            modifier = Modifier.height(52.dp)
                        ) {
                            if (state.submitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("开始评估")
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.offline) {
                Surface(color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "离线演示模式：使用内置症状库",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = viewModel::load) { Text("连接服务器") }
                    }
                }
            }
            OutlinedTextField(
                value = state.keyword,
                onValueChange = viewModel::setKeyword,
                placeholder = { Text("搜索症状，如：发热、头痛") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.category == null,
                    onClick = { viewModel.setCategory(null) },
                    label = { Text("全部") }
                )
                state.categories.forEach { c ->
                    FilterChip(
                        selected = state.category == c,
                        onClick = { viewModel.setCategory(c) },
                        label = { Text(c) }
                    )
                }
            }

            val loadError = state.loadError
            when {
                state.loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                loadError != null -> Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(loadError, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = viewModel::load) { Text("重试") }
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.filtered, key = { it.id }) { symptom ->
                        val checked = symptom.id in state.selected
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggle(symptom.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (checked) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { viewModel.toggle(symptom.id) }
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        symptom.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    val desc = symptom.description
                                    if (!desc.isNullOrBlank()) {
                                        Text(
                                            desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (symptom.defaultSeverity >= 2) {
                                    Text(
                                        if (symptom.defaultSeverity >= 3) "需警惕" else "较常见",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                    if (state.filtered.isEmpty()) {
                        item {
                            Text(
                                "没有匹配的症状，换个关键词试试",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}