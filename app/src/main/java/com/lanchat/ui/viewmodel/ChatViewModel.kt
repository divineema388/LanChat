package com.lanchat.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lanchat.model.ConnectionState
import com.lanchat.model.Message
import com.lanchat.model.MessageType
import com.lanchat.model.User
import com.lanchat.network.ChatClient
import com.lanchat.network.ChatServer
import com.lanchat.network.UserDiscovery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class ChatViewModel : ViewModel() {
    private val _connectionState = MutableStateFlow(
        ConnectionState(
            username = "",
            isConnected = false,
            isHosting = false,
            connectedTo = ""
        )
    )
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _discoveredUsers = MutableStateFlow<List<User>>(emptyList())
    val discoveredUsers: StateFlow<List<User>> = _discoveredUsers.asStateFlow()

    private var chatServer: ChatServer? = null
    private var chatClient: ChatClient? = null
    private var userDiscovery: UserDiscovery? = null

    fun setUsername(username: String) {
        _connectionState.value = _connectionState.value.copy(username = username)
    }

    fun startServer() = viewModelScope.launch {
        try {
            chatServer?.stop()
            chatServer = ChatServer(
                username = _connectionState.value.username,
                onMessageReceived = { message ->
                    _messages.value = _messages.value + message
                },
                onUserConnected = { user ->
                    _connectionState.value = _connectionState.value.copy(
                        isConnected = true,
                        connectedTo = user.username
                    )
                },
                onUserDisconnected = {
                    _connectionState.value = _connectionState.value.copy(
                        isConnected = false,
                        connectedTo = ""
                    )
                }
            )
            chatServer?.start()
            _connectionState.value = _connectionState.value.copy(isHosting = true)
            
            // Start discovery so others can find this server
            userDiscovery = UserDiscovery(_connectionState.value.username)
            userDiscovery?.startAdvertising()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun discoverUsers() = viewModelScope.launch {
        try {
            userDiscovery = UserDiscovery(_connectionState.value.username)
            userDiscovery?.discoverUsers { users ->
                _discoveredUsers.value = users
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun connectToUser(user: User) = viewModelScope.launch {
        try {
            chatClient?.disconnect()
            chatClient = ChatClient(
                username = _connectionState.value.username,
                onMessageReceived = { message ->
                    _messages.value = _messages.value + message
                },
                onConnected = {
                    _connectionState.value = _connectionState.value.copy(
                        isConnected = true,
                        connectedTo = user.username
                    )
                },
                onDisconnected = {
                    _connectionState.value = _connectionState.value.copy(
                        isConnected = false,
                        connectedTo = ""
                    )
                }
            )
            chatClient?.connect(user.ip, user.port)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendMessage(content: String) = viewModelScope.launch {
        try {
            val message = Message(
                id = UUID.randomUUID().toString(),
                sender = _connectionState.value.username,
                content = content,
                type = MessageType.TEXT,
                timestamp = Date()
            )
            
            _messages.value = _messages.value + message
            
            if (_connectionState.value.isHosting) {
                chatServer?.broadcastMessage(message)
            } else {
                chatClient?.sendMessage(message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendFile(filePath: String) = viewModelScope.launch {
        try {
            val message = Message(
                id = UUID.randomUUID().toString(),
                sender = _connectionState.value.username,
                content = filePath,
                type = MessageType.FILE,
                timestamp = Date()
            )
            
            _messages.value = _messages.value + message
            
            if (_connectionState.value.isHosting) {
                chatServer?.broadcastMessage(message)
            } else {
                chatClient?.sendMessage(message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cleanup() {
        chatServer?.stop()
        chatClient?.disconnect()
        userDiscovery?.stop()
    }
}