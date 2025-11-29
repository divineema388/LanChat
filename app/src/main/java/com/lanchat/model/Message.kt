package com.lanchat.model

import java.util.*

enum class MessageType {
    TEXT, IMAGE, FILE
}

data class Message(
    val id: String,
    val sender: String,
    val content: String,
    val type: MessageType,
    val timestamp: Date
)