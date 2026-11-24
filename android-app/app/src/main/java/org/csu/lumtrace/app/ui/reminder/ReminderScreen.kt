package org.csu.lumtrace.app.ui.reminder

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.ViewTreeObserver
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.csu.lumtrace.app.data.ApiClient
import org.csu.lumtrace.app.data.MedicineReminder
import org.csu.lumtrace.app.data.ReminderStore
import org.csu.lumtrace.app.notify.ReminderNotifier
import org.csu.lumtrace.app.notify.ReminderScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReminderUiState(
    val loading: Boolean = true,
    val reminders: List<MedicineReminder> = emptyList(),
    val checkins: Set<String> = emptySet(),
    val showAdd: Boolean = false
)

class ReminderViewModel : ViewModel() {

    var uiState by mutableStateOf(ReminderUiState())
        private set

    private val context get() = ApiClient.appContext

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val list = ReminderStore.load(context)
            val checks = ReminderStore.loadCheckins(context)
            uiState = uiState.copy(loading = false, reminders = list, checkins = checks)
        }
    }

    fun openAdd() {
        uiState = uiState.copy(showAdd = true)
    }

    fun closeAdd() {
        uiState = uiState.copy(showAdd = false)
    }

    fun add(name: String, dosage: String, times: List<String>) {
        if (name.isBlank() || times.isEmpty()) return
        viewModelScope.launch {
            val reminder = MedicineReminder(
                id = System.currentTimeMillis(),
                name = name.trim(),
                dosage = dosage.trim(),
                times = times,
                enabled = true
            )
            ReminderStore.upsert(context, reminder)
            ReminderScheduler.scheduleAll(context, reminder)
            uiState = uiState.copy(showAdd = false)
            load()
        }
    }

    fun toggle(reminder: MedicineReminder) {
        viewModelScope.launch {
            val next = reminder.copy(enabled = !reminder.enabled)
            ReminderStore.upsert(context, next)
            ReminderScheduler.scheduleAll(context, next)
            load()
        }
    }

    fun remove(id: Long) {
        viewModelScope.launch {
            uiState.reminders.firstOrNull { it.id == id }?.let {
                ReminderScheduler.cancelAll(context, it)
            }
            ReminderStore.remove(context, id)
            load()
        }
    }

    fun checkIn(reminderId: Long, time: String) {
        viewModelScope.launch {
            val day = SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(Date())
            ReminderStore.addCheckin(context, ReminderStore.checkinKey(reminderId, time, day))
            load()
        }
    }
}

private fun timeLabel(time: String): String = when (time) {
    "08:00" -> "08:00 早餐"
    "12:30" -> "12:30 午餐"
    "18:30" -> "18:30 晚餐"
    "21:30" -> "21:30 睡前"
    else -> time
}

private fun todayKey(): String =
    SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(Date())

/** 精确闹钟权限（Android 14+ 默认不授予）；未授权时提醒会退化为不精确闹钟。 */
private fun checkExactAlarm(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    onBack: () -> Unit,
    viewModel: ReminderViewModel = viewModel()
) {
    val state = viewModel.uiState
    val context = LocalContext.current
    val today = remember { todayKey() }

    var hasPermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // 从系统设置返回时重新检查精确闹钟权限
    var exactAllowed by remember { mutableStateOf(checkExactAlarm(context)) }
    val rootView = LocalView.current
    DisposableEffect(rootView) {
        val listener = ViewTreeObserver.OnWindowFocusChangeListener { focused ->
            if (focused) exactAllowed = checkExactAlarm(context)
        }
        rootView.viewTreeObserver.addOnWindowFocusChangeListener(listener)
        onDispose { rootView.viewTreeObserver.removeOnWindowFocusChangeListener(listener) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("用药提醒") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("返回") }
                },
                actions = {
                    TextButton(onClick = {
                        ReminderNotifier.show(context, 999999L, "测试药品", "1 片", "现在")
                    }) {
                        Text("测试通知")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = viewModel::openAdd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp)
                ) {
                    Text("添加用药提醒", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!hasPermission) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "通知权限未开启，到点无法弹出提醒。",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }) {
                            Text("开启通知权限")
                        }
                    }
                }
            }

            if (!exactAllowed) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "未开启精确提醒，提醒可能延后几分钟。",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(
                                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                        Uri.parse("package:" + context.packageName)
                                    )
                                )
                            }
                        }) {
                            Text("去开启精确提醒")
                        }
                    }
                }
            }

            when {
                state.loading -> Unit

                state.reminders.isEmpty() -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("还没有用药提醒", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "按医嘱录入药品和服用时间，到点会在本机弹出通知提醒服药",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.reminders.size) { index ->
                        val reminder = state.reminders[index]
                        ReminderCard(
                            reminder = reminder,
                            checkins = state.checkins,
                            today = today,
                            onToggle = { viewModel.toggle(reminder) },
                            onRemove = { viewModel.remove(reminder.id) },
                            onCheckIn = { time -> viewModel.checkIn(reminder.id, time) }
                        )
                    }
                }
            }
        }

        if (state.showAdd) {
            AddReminderDialog(
                onDismiss = viewModel::closeAdd,
                onConfirm = viewModel::add
            )
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: MedicineReminder,
    checkins: Set<String>,
    today: String,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
    onCheckIn: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        reminder.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (reminder.dosage.isNotBlank()) {
                        Text(
                            "每次 " + reminder.dosage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(checked = reminder.enabled, onCheckedChange = { onToggle() })
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "点击时间点完成今日打卡：",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                reminder.times.forEach { time ->
                    val done = checkins.contains(
                        ReminderStore.checkinKey(reminder.id, time, today)
                    )
                    FilterChip(
                        selected = done,
                        onClick = { onCheckIn(time) },
                        label = { Text(if (done) "已服 " + time else time) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (reminder.enabled) "提醒已开启" else "提醒已暂停",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onRemove) { Text("删除") }
            }
        }
    }
}

@Composable
private fun AddReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var times by remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加用药提醒") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("药品名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("剂量，如 1 片 / 5ml") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text("提醒时间（可多选）", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReminderStore.PRESET_TIMES.forEach { t ->
                        val picked = t in times
                        FilterChip(
                            selected = picked,
                            onClick = {
                                times = if (picked) times - t else times + t
                            },
                            label = { Text(timeLabel(t)) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "提醒由本机闹钟触发，不依赖网络；请保持通知权限开启。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, dosage, times.sorted()) },
                enabled = name.isNotBlank() && times.isNotEmpty()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}