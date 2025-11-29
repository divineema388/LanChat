package com.lanchat.network

import com.google.gson.Gson
import com.lanchat.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface

class UserDiscovery(private val username: String) {
    private val gson = Gson()
    private val discoveryPort = 8888
    private var isAdvertising = false
    private var isDiscovering = false
    private var advertisingSocket: DatagramSocket? = null
    private var discoverySocket: DatagramSocket? = null

    suspend fun startAdvertising() = withContext(Dispatchers.IO) {
        try {
            if (isAdvertising) return@withContext
            
            advertisingSocket = DatagramSocket().apply {
                broadcast = true
            }
            
            isAdvertising = true
            
            // Broadcast presence every 5 seconds
            while (isAdvertising) {
                broadcastPresence()
                Thread.sleep(5000)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            advertisingSocket?.close()
        }
    }

    suspend fun discoverUsers(onUsersDiscovered: (List<User>) -> Unit) = withContext(Dispatchers.IO) {
        try {
            if (isDiscovering) return@withContext
            
            discoverySocket = DatagramSocket(discoveryPort).apply {
                broadcast = true
                soTimeout = 10000 // 10 second timeout
            }
            
            isDiscovering = true
            val discoveredUsers = mutableListOf<User>()
            
            // Listen for broadcasts for 10 seconds
            val startTime = System.currentTimeMillis()
            while (isDiscovering && System.currentTimeMillis() - startTime < 10000) {
                try {
                    val buffer = ByteArray(1024)
                    val packet = DatagramPacket(buffer, buffer.size)
                    discoverySocket?.receive(packet)
                    
                    val message = String(packet.data, 0, packet.length)
                    if (message.startsWith("LANCHAT:")) {
                        val userJson = message.removePrefix("LANCHAT:")
                        val user = gson.fromJson(userJson, User::class.java)
                        
                        // Don't add ourselves
                        if (user.username != username) {
                            discoveredUsers.add(user)
                        }
                    }
                } catch (e: Exception) {
                    // Timeout or other error, continue
                }
            }
            
            onUsersDiscovered(discoveredUsers.distinctBy { it.username })
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            discoverySocket?.close()
            isDiscovering = false
        }
    }

    private fun broadcastPresence() {
        try {
            val user = User(
                username = username,
                ip = getLocalIpAddress(),
                port = 8080
            )
            
            val userJson = gson.toJson(user)
            val message = "LANCHAT:$userJson"
            val buffer = message.toByteArray()
            
            val broadcastAddress = InetAddress.getByName("255.255.255.255")
            val packet = DatagramPacket(
                buffer, 
                buffer.size, 
                broadcastAddress, 
                discoveryPort
            )
            
            advertisingSocket?.send(packet)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getLocalIpAddress(): String {
        return try {
            NetworkInterface.getNetworkInterfaces().toList()
                .flatMap { it.inetAddresses.toList() }
                .firstOrNull { address ->
                    !address.isLoopbackAddress && address.hostAddress.contains(".")
                }?.hostAddress ?: "127.0.0.1"
        } catch (e: Exception) {
            "127.0.0.1"
        }
    }

    fun stop() {
        isAdvertising = false
        isDiscovering = false
        advertisingSocket?.close()
        discoverySocket?.close()
    }
}