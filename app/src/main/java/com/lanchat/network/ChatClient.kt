package com.lanchat.network

import com.google.gson.Gson
import com.lanchat.model.Message
import com.lanchat.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class ChatClient(
    private val username: String,
    private val onMessageReceived: (Message) -> Unit,
    private val onConnected: () -> Unit,
    private val onDisconnected: () -> Unit
) {
    private val gson = Gson()
    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()
    
    private var serverIp: String = ""
    private var serverPort: Int = 8080

    suspend fun connect(ip: String, port: Int) = withContext(Dispatchers.IO) {
        try {
            serverIp = ip
            serverPort = port
            
            // Send connect request
            val user = User(username, getLocalIp(), 0)
            val json = gson.toJson(user)
            val requestBody = json.toRequestBody(JSON)
            
            val request = Request.Builder()
                .url("http://$serverIp:$serverPort/connect")
                .post(requestBody)
                .build()
            
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    onConnected()
                } else {
                    throw IOException("Connection failed: ${response.code}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun sendMessage(message: Message) = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(message)
            val requestBody = json.toRequestBody(JSON)
            
            val request = Request.Builder()
                .url("http://$serverIp:$serverPort/message")
                .post(requestBody)
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Failed to send message: ${response.code}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            val user = User(username, getLocalIp(), 0)
            val json = gson.toJson(user)
            val requestBody = json.toRequestBody(JSON)
            
            val request = Request.Builder()
                .url("http://$serverIp:$serverPort/disconnect")
                .post(requestBody)
                .build()
            
            client.newCall(request).execute()
            onDisconnected()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getLocalIp(): String {
        return try {
            // This is a simplified implementation
            // In a real app, you'd need to get the actual local IP
            "192.168.1.100" // Placeholder
        } catch (e: Exception) {
            "127.0.0.1"
        }
    }
}