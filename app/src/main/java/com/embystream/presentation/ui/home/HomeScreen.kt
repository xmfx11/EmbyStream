package com.embystream.presentation.ui.home

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ArrowBack
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
import com.embystream.utils.ImageUrlBuilder
import com.embystream.utils.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    parentId: String? = null,
    parentName: String? = null,
    filterTag: String? = null,
    filterPersonId: String? = null,
    filterPersonName: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var items by remember { mutableStateOf<List<EmbyItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var serverUrl by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    
    val title = when {
        !filterTag.isNullOrEmpty() -> "标签: $filterTag"
        !filterPersonName.isNullOrEmpty() -> "演员: $filterPersonName"
        !parentName.isNullOrEmpty() -> parentName
        else -> "媒体库"
    }
    
    LaunchedEffect(Unit) {
        serverUrl = TokenManager.getServer() ?: ""
        userId = TokenManager.getUserId() ?: ""
        val token = TokenManager.getToken() ?: ""
        
        if (serverUrl.isEmpty() || userId.isEmpty()) {
            isLoading = false
            return@LaunchedEffect
        }
        
        isLoading = true
        try {
            val apiService = NetworkModule.createApiService(serverUrl, token)
            val repository = EmbyRepository(apiService)
            
            val result = withContext(Dispatchers.IO) {
                when {
                    !filterTag.isNullOrEmpty() -> {
                        repository.getItemsByTag(userId, filterTag)
                    }
                    !filterPersonId.isNullOrEmpty() -> {
                        repository.getItemsByPerson(userId, filterPersonId)
                    }
                    else -> {
                        if (parentId.isNullOrEmpty()) {
                            repository.getViews(userId)
                        } else {
                            repository.getItems(userId, parentId)
                        }
                    }
                }
            }
            
            result.onSuccess { itemList ->
                items = itemList
            }.onFailure { error ->
                Toast.makeText(context, "加载失败: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "错误: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (parentId != null || filterTag != null || filterPersonId != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
                actions = {
                    if (parentId == null && filterTag == null && filterPersonId == null) {
                        IconButton(onClick = {
                            navController.navigate(Screen.Settings.route)
                        }) {
                            Icon(Icons.Default.Settings, contentDescription = "设置")
                        }
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
            } else if (items.isEmpty()) {
                Text(
                    text = "暂无内容",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.Id ?: "" }) { item ->
                        MediaGridItem(
                            item = item,
                            serverUrl = serverUrl,
                            onClick = {
                                if (parentId == null && filterTag == null && filterPersonId == null) {
                                    navController.navigate(
                                        Screen.LibraryItems.createRoute(
                                            item.Id ?: "",
                                            item.Name ?: ""
                                        )
                                    )
                                } else {
                                    navController.navigate(
                                        Screen.Detail.createRoute(item.Id ?: "")
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaGridItem(
    item: EmbyItem,
    serverUrl: String,
    onClick: () -> Unit
) {
    val imageUrl = ImageUrlBuilder.buildImageUrl(
        serverUrl,
        item.Id,
        item.ImageTags?.Primary
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = item.Name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.Name ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = item.Name ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.ProductionYear != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.ProductionYear.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
