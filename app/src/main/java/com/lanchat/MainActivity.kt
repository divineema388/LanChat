package com.lanchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.lanchat.ui.navigation.AppNavigation
import com.lanchat.ui.theme.LANChatTheme
import com.lanchat.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            LANChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(chatViewModel = chatViewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        chatViewModel.cleanup()
    }
}