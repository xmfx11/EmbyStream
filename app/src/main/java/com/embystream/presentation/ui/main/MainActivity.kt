package com.embystream.presentation.ui.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.embystream.data.local.TokenManager
import com.embystream.presentation.navigation.NavGraph
import com.embystream.presentation.navigation.Screen
import com.embystream.presentation.theme.EmbyStreamTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmbyStreamTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val navController = rememberNavController()
    var startDestination by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.coroutineScope {
            launch(Dispatchers.IO) {
                try {
                    val isLoggedIn = TokenManager.isLoggedIn()
                    withContext(Dispatchers.Main) {
                        startDestination = if (isLoggedIn) {
                            Screen.Home.route
                        } else {
                            Screen.Login.route
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        startDestination = Screen.Login.route
                    }
                }
            }
        }
    }
    
    if (startDestination != null) {
        NavGraph(
            navController = navController,
            startDestination = startDestination!!
        )
    }
}
