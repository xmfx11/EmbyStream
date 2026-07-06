package com.embystream.presentation.ui.login

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.embystream.data.local.TokenManager
import com.embystream.data.repository.EmbyRepository
import com.embystream.utils.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var serverUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        val savedServer = TokenManager.getServer()
        val savedToken = TokenManager.getToken()
        if (!savedServer.isNullOrEmpty() && !savedToken.isNullOrEmpty()) {
            serverUrl = savedServer
            try {
                val apiService = NetworkModule.createApiService(serverUrl, savedToken)
                val repository = EmbyRepository(apiService)
                val userId = TokenManager.getUserId() ?: return@LaunchedEffect
                val result = repository.getViews(userId)
                if (result.isSuccess) {
                    onLoginSuccess()
                    return@LaunchedEffect
                }
            } catch (_: Exception) {
            }
        }
    }
    
    fun handleLogin() {
        if (serverUrl.isBlank() || username.isBlank()) {
            Toast.makeText(context, "请填写服务器地址和用户名", Toast.LENGTH_SHORT).show()
            return
        }
        
        isLoading = true
        coroutineScope.launch {
            try {
                val apiService = NetworkModule.createApiService(serverUrl)
                val repository = EmbyRepository(apiService)
                val result = withContext(Dispatchers.IO) {
                    repository.login(username, password)
                }
                
                result.onSuccess { loginResponse ->
                    val token = loginResponse.AccessToken
                    val user = loginResponse.User
                    val userId = user?.Id
                    if (!token.isNullOrEmpty() && !userId.isNullOrEmpty()) {
                        TokenManager.save(serverUrl, token, userId)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "登录失败：服务器返回无效的响应", Toast.LENGTH_LONG).show()
                        }
                    }
                }.onFailure { error ->
                    withContext(Dispatchers.Main) {
                        val errorMsg = when {
                            error.message?.contains("Unable to resolve host") == true -> 
                                "无法连接到服务器，请检查地址是否正确"
                            error.message?.contains("connect timed out") == true -> 
                                "连接超时，请检查服务器是否运行"
                            error.message?.contains("401") == true -> 
                                "用户名或密码错误"
                            error.message?.contains("404") == true -> 
                                "服务器地址错误或不是 Emby 服务器"
                            else -> "登录失败: ${error.message}"
                        }
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "连接错误: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EmbyStream 登录") }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "欢迎使用 EmbyStream",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Text(
                    text = "请连接到您的 Emby 服务器",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))
                
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("服务器地址") },
                    placeholder = { Text("http://192.168.1.100:8096") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("用户名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { handleLogin() }
                    )
                )
                
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))
                
                Button(
                    onClick = { handleLogin() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(if (isLoading) "登录中..." else "登录")
                }
            }
        }
    }
}
