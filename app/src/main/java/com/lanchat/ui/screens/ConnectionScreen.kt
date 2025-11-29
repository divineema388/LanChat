package com.lanchat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lanchat.ui.viewmodel.ChatViewModel
import com.lanchat.ui.theme.OnlineGreen
import com.lanchat.ui.theme.OfflineGray
import kotlinx.coroutines.launch

@Composable
fun ConnectionScreen(
    chatViewModel: ChatViewModel,
    onConnected: () -> Unit,
    onBack: () -> Unit
) {
    val connectionState by chatViewModel.connectionState.collectAsState()
    val discoveredUsers by chatViewModel.discoveredUsers.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(connectionState.isConnected) {
        if (connectionState.isConnected) {
            onConnected()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Connection",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Connection status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (connectionState.isConnected) OnlineGreen.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (connectionState.isConnected) Icons.Default.CheckCircle
                    else Icons.Default.Schedule,
                    contentDescription = "Status",
                    tint = if (connectionState.isConnected) OnlineGreen else OfflineGray
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (connectionState.isConnected) "Connected to ${connectionState.connectedTo}"
                        else "Searching for users...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Username: ${connectionState.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        chatViewModel.startServer()
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !connectionState.isHosting
            ) {
                Icon(Icons.Default.Computer, contentDescription = "Host")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Host Chat")
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        chatViewModel.discoverUsers()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Default.Search, contentDescription = "Discover")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Discover")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Discovered users list
        Text(
            text = "Available Users",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (discoveredUsers.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = "No users",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No users found\nClick 'Discover' to search for users on your network",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(discoveredUsers) { user ->
                    UserListItem(
                        user = user,
                        onConnect = {
                            coroutineScope.launch {
                                chatViewModel.connectToUser(user)
                            }
                        },
                        isConnected = connectionState.connectedTo == user.username
                    )
                }
            }
        }
    }
}

@Composable
fun UserListItem(
    user: com.lanchat.model.User,
    onConnect: () -> Unit,
    isConnected: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) OnlineGreen.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User",
                tint = if (isConnected) OnlineGreen else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${user.ip}:${user.port}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            if (!isConnected) {
                Button(onClick = onConnect) {
                    Text("Connect")
                }
            } else {
                Text(
                    text = "Connected",
                    color = OnlineGreen,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}