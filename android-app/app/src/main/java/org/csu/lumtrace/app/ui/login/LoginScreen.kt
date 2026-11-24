package org.csu.lumtrace.app.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.csu.lumtrace.app.data.ApiException
import org.csu.lumtrace.app.data.AuthRepository

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null
)

class LoginViewModel : ViewModel() {

    private val repo = AuthRepository()

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onUsernameChange(value: String) {
        uiState = uiState.copy(username = value, error = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, error = null)
    }

    fun login(onSuccess: () -> Unit) {
        if (uiState.loading) return
        val username = uiState.username.trim()
        val password = uiState.password
        if (username.isBlank() || password.isBlank()) {
            uiState = uiState.copy(error = "请输入用户名和密码")
            return
        }
        viewModelScope.launch {
            uiState = uiState.copy(loading = true, error = null)
            try {
                repo.login(username, password)
                uiState = uiState.copy(loading = false)
                onSuccess()
            } catch (e: Exception) {
                uiState = uiState.copy(
                    loading = false,
                    error = if (e is ApiException) e.message else "网络异常，请检查服务器地址"
                )
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onGuest: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state = viewModel.uiState
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("溯光智诊", style = MaterialTheme.typography.headlineLarge)
            Text("AI 就医便民小助手", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(40.dp))

            OutlinedTextField(
                value = state.username,
                onValueChange = viewModel::onUsernameChange,
                label = { Text("用户名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("密码") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            state.error?.let { error ->
                Spacer(Modifier.height(12.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.login(onLoggedIn) },
                enabled = !state.loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (state.loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("登 录", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.height(16.dp))
            TextButton(
                onClick = onGuest,
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("暂不登录，离线体验")
            }
            Text("本应用仅供参考，不能替代执业医师", style = MaterialTheme.typography.bodySmall)
        }
    }
}