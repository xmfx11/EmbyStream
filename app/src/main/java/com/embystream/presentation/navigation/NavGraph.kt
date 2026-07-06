package com.embystream.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.embystream.presentation.ui.detail.DetailScreen
import com.embystream.presentation.ui.home.HomeScreen
import com.embystream.presentation.ui.login.LoginScreen
import com.embystream.presentation.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Detail : Screen("detail/{itemId}") {
        fun createRoute(itemId: String) = "detail/$itemId"
    }
    object TagItems : Screen("tag/{tag}") {
        fun createRoute(tag: String) = "tag/$tag"
    }
    object PersonItems : Screen("person/{personId}/{personName}") {
        fun createRoute(personId: String, personName: String) = "person/$personId/$personName"
    }
    object LibraryItems : Screen("library/{parentId}/{parentName}") {
        fun createRoute(parentId: String, parentName: String) = "library/$parentId/$parentName"
    }
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController
            )
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            DetailScreen(
                itemId = itemId,
                navController = navController
            )
        }
        composable(
            route = Screen.TagItems.route,
            arguments = listOf(navArgument("tag") { type = NavType.StringType })
        ) { backStackEntry ->
            val tag = backStackEntry.arguments?.getString("tag") ?: ""
            HomeScreen(
                navController = navController,
                filterTag = tag
            )
        }
        composable(
            route = Screen.PersonItems.route,
            arguments = listOf(
                navArgument("personId") { type = NavType.StringType },
                navArgument("personName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getString("personId") ?: ""
            val personName = backStackEntry.arguments?.getString("personName") ?: ""
            HomeScreen(
                navController = navController,
                filterPersonId = personId,
                filterPersonName = personName
            )
        }
        composable(
            route = Screen.LibraryItems.route,
            arguments = listOf(
                navArgument("parentId") { type = NavType.StringType },
                navArgument("parentName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val parentId = backStackEntry.arguments?.getString("parentId") ?: ""
            val parentName = backStackEntry.arguments?.getString("parentName") ?: ""
            HomeScreen(
                navController = navController,
                parentId = parentId,
                parentName = parentName
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
