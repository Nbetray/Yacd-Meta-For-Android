package cn.nbetray.data.websocket

import cn.nbetray.data.local.ApiConfig
import cn.nbetray.data.model.ConnectionsResponse
import cn.nbetray.data.model.LogEntry
import cn.nbetray.data.model.MemoryData
import cn.nbetray.data.model.TrafficData
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClashWebSocketManager @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    private fun buildWebSocketUrl(config: ApiConfig, path: String): String {
        val baseUrl = config.baseUrl
            .replace("http://", "ws://")
            .replace("https://", "wss://")
            .trimEnd('/')

        val tokenParam = if (config.secret.isNotEmpty()) {
            "?token=${config.secret}"
        } else {
            ""
        }

        return "$baseUrl/$path$tokenParam"
    }

    fun trafficFlow(config: ApiConfig): Flow<TrafficData> = callbackFlow {
        val url = buildWebSocketUrl(config, "traffic")
        val request = Request.Builder().url(url).build()

        val webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val traffic = gson.fromJson(text, TrafficData::class.java)
                    trySend(traffic)
                } catch (e: Exception) {
                    // Ignore parse errors
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                close()
            }
        })

        awaitClose {
            webSocket.close(1000, "Flow closed")
        }
    }

    fun connectionsFlow(config: ApiConfig): Flow<ConnectionsResponse> = callbackFlow {
        val url = buildWebSocketUrl(config, "connections")
        val request = Request.Builder().url(url).build()

        val webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val connections = gson.fromJson(text, ConnectionsResponse::class.java)
                    trySend(connections)
                } catch (e: Exception) {
                    // Ignore parse errors
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                close()
            }
        })

        awaitClose {
            webSocket.close(1000, "Flow closed")
        }
    }

    fun logsFlow(config: ApiConfig, level: String = "info"): Flow<LogEntry> = callbackFlow {
        val baseUrl = config.baseUrl
            .replace("http://", "ws://")
            .replace("https://", "wss://")
            .trimEnd('/')

        val params = mutableListOf("level=$level")
        if (config.secret.isNotEmpty()) {
            params.add("token=${config.secret}")
        }
        val url = "$baseUrl/logs?${params.joinToString("&")}"

        val request = Request.Builder().url(url).build()

        val webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val log = gson.fromJson(text, LogEntry::class.java)
                    // Clash API doesn't return timestamp, so we set it here
                    val logWithTimestamp = log.copy(timestamp = System.currentTimeMillis())
                    trySend(logWithTimestamp)
                } catch (e: Exception) {
                    // Ignore parse errors
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                close()
            }
        })

        awaitClose {
            webSocket.close(1000, "Flow closed")
        }
    }

    fun memoryFlow(config: ApiConfig): Flow<MemoryData> = callbackFlow {
        val url = buildWebSocketUrl(config, "memory")
        val request = Request.Builder().url(url).build()

        val webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val memory = gson.fromJson(text, MemoryData::class.java)
                    trySend(memory)
                } catch (e: Exception) {
                    // Ignore parse errors
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                close()
            }
        })

        awaitClose {
            webSocket.close(1000, "Flow closed")
        }
    }
}
