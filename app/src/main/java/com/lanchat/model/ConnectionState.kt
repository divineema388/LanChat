package com.lanchat.model

data class ConnectionState(
    val username: String,
    val isConnected: Boolean,
    val isHosting: Boolean,
    val connectedTo: String
)