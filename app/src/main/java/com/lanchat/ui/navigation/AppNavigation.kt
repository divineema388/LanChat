package com.lanchat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lanchat.ui.viewmodel.ChatViewModel
import com.lanchat.ui.screens.ConnectionScreen
import com.lanchat.ui.screens.ChatScreen
import com.lanchat.ui.screens.UsernameScreen

sealed class Screen(val route: String) {
    object Username : Screen("username")
    object Connection : Screen("connection")
    object Chat : Screen("chat")
}

@Composable
fun AppNavigation(chatViewModel: ChatViewModel) {
    val navController = rememberNavController()
    
    // Reset navigation when connection is lost
    LaunchedEffect(chatViewModel.connectionState) {
        if (!chatViewModel.connectionState.isConnected && 
            chatViewModel.connectionState.username.isNotEmpty()) {
            navController.navigate(Screen.Connection.route) {
                popUpTo(Screen.Connection.route) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Username.route
    ) {
        composable(Screen.Username.route) {
            UsernameScreen(
                onUsernameSubmitted = { username ->
                    chatViewModel.setUsername(username)
                    navController.navigate(Screen.Connection.route)
                }
            )
        }
        
        composable(Screen.Connection.route) {
            ConnectionScreen(
                chatViewModel = chatViewModel,
                onConnected = {
                    navController.navigate(Screen.Chat.route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Chat.route) {
            ChatScreen(
                chatViewModel = chatViewModel,
                onDisconnect = {
                    navController.popBackStack(Screen.Connection.route, false)
                }
            )
        }
    }
}