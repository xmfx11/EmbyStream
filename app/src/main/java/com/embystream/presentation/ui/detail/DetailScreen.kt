package com.embystream.presentation.ui.detail

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.embystream.data.local.TokenManager
import com.embystream.data.model.EmbyItem
import com.embystream.data.repository.EmbyRepository
import com.embystream.presentation.navigation.Screen
import com.embystream.presentation.ui.player.PlayerActivity
import com.embystream.utils.ImageUrlBuilder
import com.embystream.utils.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    itemId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var item by remember { mutableStateOf<EmbyItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var serverUrl by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    
    LaunchedEffect(itemId) {
        serverUrl = TokenManager.getServer() ?: ""
        userId = TokenManager.getUserId() ?: ""
        
        if (serverUrl.isEmpty() || userId.isEmpty() || itemId.isEmpty()) {
            isLoading = false
            return@LaunchedEffect
        }
        
        loadItemDetail()
    }
    
    fun loadItemDetail() {
        isLoading = true
        coroutineScope.launch {
            try {
                val apiService = NetworkModule.createApiService(serverUrl)
                val repository = EmbyRepository(apiService)
                
                val result = withContext(Dispatchers.IO) {
                    val itemsResult = repository.getItems(userId, itemId)
                    itemsResult.mapCatching { items ->
                        items.firstOrNull() ?: throw Exception("未找到项目")
                    }
                }
                
                result.onSuccess { embyItem ->
                    item = embyItem
                }.onFailure { error ->
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "加载失败: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "错误: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }
    
    fun playVideo() {
        if (item?.Id.isNullOrEmpty()) return
        val intent = Intent(context, PlayerActivity::class.java).apply {
            putExtra("item_id", item?.Id)
        }
        context.startActivity(intent)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        item?.Name ?: "详情",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (item == null) {
                Text(
                    text = "加载失败",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                val embyItem = item!!
                val imageUrl = ImageUrlBuilder.buildImageUrl(
                    serverUrl,
                    embyItem.Id,
                    embyItem.ImageTags?.Primary
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        if (!imageUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = embyItem.Name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center)
                        ) {
                            Button(
                                onClick = { playVideo() },
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("播放")
                            }
                        }
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = embyItem.Name ?: "",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (embyItem.ProductionYear != null) {
                                Text(
                                    text = embyItem.ProductionYear.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (embyItem.CommunityRating != null) {
                                Text(
                                    text = "★ ${String.format("%.1f", embyItem.CommunityRating)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        if (!embyItem.Overview.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "简介",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = embyItem.Overview,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        if (!embyItem.Tags.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "标签",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                embyItem.Tags.take(10).forEach { tag ->
                                    AssistChip(
                                        onClick = {
                                            navController.navigate(
                                                Screen.TagItems.createRoute(tag)
                                            )
                                        },
                                        label = { Text(tag) }
                                    )
                                }
                            }
                        }
                        
                        if (!embyItem.People.isNullOrEmpty()) {
                            val actors = embyItem.People.filter { 
                                it.Role == "Actor" || it.Role == "演员" 
                            }
                            if (actors.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "演员",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    actors.take(10).forEach { person ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    navController.navigate(
                                                        Screen.PersonItems.createRoute(
                                                            person.Id ?: "",
                                                            person.Name ?: ""
                                                        )
                                                    )
                                                }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val personImageUrl = ImageUrlBuilder.buildPersonImageUrl(
                                                serverUrl,
                                                person.Id,
                                                person.PrimaryImageTag
                                            )
                                            if (!personImageUrl.isNullOrEmpty()) {
                                                AsyncImage(
                                                    model = personImageUrl,
                                                    contentDescription = person.Name,
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .clip(CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = (person.Name?.firstOrNull() ?: '?').toString(),
                                                        style = MaterialTheme.typography.titleMedium
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = person.Name ?: "",
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
