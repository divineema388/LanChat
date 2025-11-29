package com.lanchat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lanchat.model.Message
import com.lanchat.model.MessageType
import com.lanchat.ui.viewmodel.ChatViewModel
import com.lanchat.ui.theme.ChatBlue
import com.lanchat.ui.theme.ChatGreen
import com.lanchat.ui.theme.ChatGray
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    onDisconnect: () -> Unit
) {
    val connectionState by chatViewModel.connectionState.collectAsState()
    val messages by chatViewModel.messages.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Header
        ChatHeader(
            connectionState = connectionState,
            onDisconnect = onDisconnect
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Messages list
        MessagesList(
            messages = messages,
            currentUsername = connectionState.username
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Message input
        MessageInput(
            onSendMessage = { message ->
                coroutineScope.launch {
                    chatViewModel.sendMessage(message)
                }
            },
            onSendFile = { filePath ->
                coroutineScope.launch {
                    chatViewModel.sendFile(filePath)
                }
            }
        )
    }
}

@Composable
fun ChatHeader(
    connectionState: com.lanchat.model.ConnectionState,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "LAN Chat",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Connected to: ${connectionState.connectedTo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            IconButton(
                onClick = onDisconnect,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = "Disconnect")
            }
        }
    }
}

@Composable
fun MessagesList(
    messages: List<Message>,
    currentUsername: String
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages) { message ->
            MessageBubble(
                message = message,
                isOwnMessage = message.sender == currentUsername
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isOwnMessage: Boolean
) {
    val alignment = if (isOwnMessage) Alignment.End else Alignment.Start
    val bubbleColor = if (isOwnMessage) ChatBlue else ChatGray
    val textColor = if (isOwnMessage) Color.White else Color.Black

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(bubbleColor)
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            if (!isOwnMessage) {
                Text(
                    text = message.sender,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            when (message.type) {
                MessageType.TEXT -> {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor
                    )
                }
                MessageType.IMAGE -> {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(message.content)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Shared image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                MessageType.FILE -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = "File",
                            tint = textColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message.content.substringAfterLast("/"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun MessageInput(
    onSendMessage: (String) -> Unit,
    onSendFile: (String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // File attachment button (placeholder - would need file picker implementation)
        IconButton(
            onClick = {
                // TODO: Implement file picker
            }
        ) {
            Icon(Icons.Default.AttachFile, contentDescription = "Attach file")
        }

        // Message input field
        OutlinedTextField(
            value = messageText,
            onValueChange = { messageText = it },
            placeholder = { Text("Type a message...") },
            modifier = Modifier.weight(1f),
            singleLine = false,
            maxLines = 3
        )

        // Send button
        IconButton(
            onClick = {
                val trimmedMessage = messageText.trim()
                if (trimmedMessage.isNotEmpty()) {
                    onSendMessage(trimmedMessage)
                    messageText = ""
                }
            },
            enabled = messageText.trim().isNotEmpty()
        ) {
            Icon(
                Icons.Default.Send,
                contentDescription = "Send message",
                tint = if (messageText.trim().isNotEmpty()) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}