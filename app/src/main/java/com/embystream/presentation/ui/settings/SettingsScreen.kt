package com.embystream.presentation.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.embystream.BuildConfig
import com.embystream.data.local.TokenManager
import com.embystream.data.repository.UpdateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var serverUrl by remember { mutableStateOf("") }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    
    LaunchedEffect(Unit) {
        serverUrl = TokenManager.getServer() ?: ""
    }
    
    fun handleLogout() {
        coroutineScope.launch {
            try {
                TokenManager.clear()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "已退出登录", Toast.LENGTH_SHORT).show()
                    onLogout()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "退出失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    fun checkForUpdates() {
        isCheckingUpdate = true
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val updateManager = UpdateManager(context)
                val result = updateManager.checkForUpdates()
                
                withContext(Dispatchers.Main) {
                    isCheckingUpdate = false
                    
                    result.onSuccess { response ->
                        if (updateManager.isUpdateAvailable(response)) {
                            val apkUrl = updateManager.getApkDownloadUrl(response)
                            updateInfo = UpdateInfo(
                                version = response.tag_name ?: "",
                                description = response.body ?: "",
                                downloadUrl = apkUrl
                            )
                            showUpdateDialog = true
                        } else {
                            Toast.makeText(context, "当前已是最新版本", Toast.LENGTH_SHORT).show()
                        }
                    }.onFailure { error ->
                        Toast.makeText(context, "检查更新失败: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isCheckingUpdate = false
                    Toast.makeText(context, "检查更新失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    fun downloadAndInstall() {
        val url = updateInfo?.downloadUrl ?: return
        isDownloading = true
        downloadProgress = 0
        
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val updateManager = UpdateManager(context)
                val result = updateManager.downloadApk(url) { progress ->
                    downloadProgress = progress
                }
                
                withContext(Dispatchers.Main) {
                    isDownloading = false
                    
                    result.onSuccess { file ->
                        showUpdateDialog = false
                        updateManager.installApk(file)
                    }.onFailure { error ->
                        Toast.makeText(context, "下载失败: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isDownloading = false
                    Toast.makeText(context, "下载失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "服务器信息",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "服务器地址",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(100.dp)
                        )
                        Text(
                            text = serverUrl,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            Button(
                onClick = { checkForUpdates() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCheckingUpdate && !isDownloading
            ) {
                if (isCheckingUpdate) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Update,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isCheckingUpdate) "检查中..." else "检查更新")
            }
            
            Button(
                onClick = { handleLogout() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("退出登录")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "EmbyStream v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
    
    if (showUpdateDialog && updateInfo != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showUpdateDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "发现新版本",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    
                    Text(
                        text = "${updateInfo?.version}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (!updateInfo?.description.isNullOrEmpty()) {
                        Text(
                            text = updateInfo?.description ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    if (isDownloading) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(text = "下载进度: $downloadProgress%")
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showUpdateDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("稍后")
                            }
                            Button(
                                onClick = { downloadAndInstall() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("立即更新")
                            }
                        }
                    }
                }
            }
        }
    }
}

data class UpdateInfo(
    val version: String,
    val description: String?,
    val downloadUrl: String?
)
