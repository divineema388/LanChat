package com.lanchat.network

import com.google.gson.Gson
import com.lanchat.model.Message
import com.lanchat.model.User
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class ChatServer(
    private val username: String,
    private val onMessageReceived: (Message) -> Unit,
    private val onUserConnected: (User) -> Unit,
    private val onUserDisconnected: () -> Unit
) : NanoHTTPD(8080) {

    private val gson = Gson()
    private val connectedClients = mutableListOf<User>()

    override fun start() {
        try {
            super.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
            println("Chat server started on port 8080")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun serve(session: IHTTPSession): Response {
        return when (session.uri) {
            "/message" -> handleMessage(session)
            "/connect" -> handleConnect(session)
            "/disconnect" -> handleDisconnect(session)
            else -> newFixedLengthResponse("Not found")
        }
    }

    private fun handleMessage(session: IHTTPSession): Response {
        return try {
            if (session.method == Method.POST) {
                val contentLength = session.headers["content-length"]?.toInt() ?: 0
                val buffer = ByteArray(contentLength)
                session.inputStream.read(buffer)
                val json = String(buffer)
                val message = gson.fromJson(json, Message::class.java)
                
                onMessageReceived(message)
                broadcastToClients(json)
                
                newFixedLengthResponse("OK")
            } else {
                newFixedLengthResponse("Method not allowed")
            }
        } catch (e: Exception) {
            newFixedLengthResponse("Error: ${e.message}")
        }
    }

    private fun handleConnect(session: IHTTPSession): Response {
        return try {
            if (session.method == Method.POST) {
                val contentLength = session.headers["content-length"]?.toInt() ?: 0
                val buffer = ByteArray(contentLength)
                session.inputStream.read(buffer)
                val json = String(buffer)
                val user = gson.fromJson(json, User::class.java)
                
                connectedClients.add(user)
                onUserConnected(user)
                
                newFixedLengthResponse("Connected")
            } else {
                newFixedLengthResponse("Method not allowed")
            }
        } catch (e: Exception) {
            newFixedLengthResponse("Error: ${e.message}")
        }
    }

    private fun handleDisconnect(session: IHTTPSession): Response {
        return try {
            if (session.method == Method.POST) {
                val contentLength = session.headers["content-length"]?.toInt() ?: 0
                val buffer = ByteArray(contentLength)
                session.inputStream.read(buffer)
                val json = String(buffer)
                val user = gson.fromJson(json, User::class.java)
                
                connectedClients.remove(user)
                onUserDisconnected()
                
                newFixedLengthResponse("Disconnected")
            } else {
                newFixedLengthResponse("Method not allowed")
            }
        } catch (e: Exception) {
            newFixedLengthResponse("Error: ${e.message}")
        }
    }

    suspend fun broadcastMessage(message: Message) = withContext(Dispatchers.IO) {
        val json = gson.toJson(message)
        broadcastToClients(json)
    }

    private fun broadcastToClients(json: String) {
        // In a real implementation, you'd send to all connected clients
        // This is a simplified version
        println("Broadcasting: $json")
    }
}