package cn.nbetray.data.repository

import cn.nbetray.data.api.ClashApi
import cn.nbetray.data.local.ApiConfig
import cn.nbetray.data.local.ApiConfigDataStore
import cn.nbetray.data.model.*
import cn.nbetray.data.websocket.ClashWebSocketManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClashRepository @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
    private val apiConfigDataStore: ApiConfigDataStore,
    private val webSocketManager: ClashWebSocketManager
) {
    private suspend fun createApi(): ClashApi {
        val config = apiConfigDataStore.currentConfig.first()
        val client = if (config.secret.isNotEmpty()) {
            okHttpClient.newBuilder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("Authorization", "Bearer ${config.secret}")
                        .build()
                    chain.proceed(request)
                }
                .build()
        } else {
            okHttpClient
        }

        return Retrofit.Builder()
            .baseUrl(config.baseUrl.trimEnd('/') + "/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ClashApi::class.java)
    }

    // Version
    suspend fun getVersion(): Result<VersionInfo> = runCatching {
        createApi().getVersion()
    }

    // Configs
    suspend fun getConfigs(): Result<ClashConfig> = runCatching {
        createApi().getConfigs()
    }

    suspend fun patchConfigs(config: ConfigPatch): Result<Unit> = runCatching {
        val response = createApi().patchConfigs(config)
        if (!response.isSuccessful) {
            throw Exception("Failed to patch config: ${response.code()}")
        }
    }

    suspend fun reloadConfigs(): Result<Unit> = runCatching {
        val response = createApi().reloadConfigs()
        if (!response.isSuccessful) {
            throw Exception("Failed to reload config: ${response.code()}")
        }
    }

    // Proxies
    suspend fun getProxies(): Result<ProxiesResponse> = runCatching {
        createApi().getProxies()
    }

    suspend fun switchProxy(groupName: String, proxyName: String): Result<Unit> = runCatching {
        val response = createApi().switchProxy(groupName, ProxyGroupSwitch(proxyName))
        if (!response.isSuccessful) {
            throw Exception("Failed to switch proxy: ${response.code()}")
        }
    }

    suspend fun testProxyDelay(proxyName: String, url: String): Result<Int> = runCatching {
        createApi().testProxyDelay(proxyName, url = url).delay
    }

    suspend fun testGroupDelay(groupName: String, url: String): Result<Map<String, Int>> = runCatching {
        createApi().testGroupDelay(groupName, url = url)
    }

    // Proxy Providers
    suspend fun getProxyProviders(): Result<ProxyProvidersResponse> = runCatching {
        createApi().getProxyProviders()
    }

    suspend fun updateProxyProvider(providerName: String): Result<Unit> = runCatching {
        val response = createApi().updateProxyProvider(providerName)
        if (!response.isSuccessful) {
            throw Exception("Failed to update provider: ${response.code()}")
        }
    }

    suspend fun healthCheckProxyProvider(providerName: String): Result<Unit> = runCatching {
        val response = createApi().healthCheckProxyProvider(providerName)
        if (!response.isSuccessful) {
            throw Exception("Failed to health check: ${response.code()}")
        }
    }

    // Rules
    suspend fun getRules(): Result<RulesResponse> = runCatching {
        createApi().getRules()
    }

    suspend fun getRuleProviders(): Result<RuleProvidersResponse> = runCatching {
        createApi().getRuleProviders()
    }

    suspend fun updateRuleProvider(providerName: String): Result<Unit> = runCatching {
        val response = createApi().updateRuleProvider(providerName)
        if (!response.isSuccessful) {
            throw Exception("Failed to update rule provider: ${response.code()}")
        }
    }

    // Connections
    suspend fun getConnections(): Result<ConnectionsResponse> = runCatching {
        createApi().getConnections()
    }

    suspend fun closeAllConnections(): Result<Unit> = runCatching {
        val response = createApi().closeAllConnections()
        if (!response.isSuccessful) {
            throw Exception("Failed to close connections: ${response.code()}")
        }
    }

    suspend fun closeConnection(connectionId: String): Result<Unit> = runCatching {
        val response = createApi().closeConnection(connectionId)
        if (!response.isSuccessful) {
            throw Exception("Failed to close connection: ${response.code()}")
        }
    }

    // Control
    suspend fun restart(): Result<Unit> = runCatching {
        val response = createApi().restart()
        if (!response.isSuccessful) {
            throw Exception("Failed to restart: ${response.code()}")
        }
    }

    suspend fun upgrade(): Result<Unit> = runCatching {
        val response = createApi().upgrade()
        if (!response.isSuccessful) {
            throw Exception("Failed to upgrade: ${response.code()}")
        }
    }

    suspend fun upgradeGeo(): Result<Unit> = runCatching {
        val response = createApi().upgradeGeo()
        if (!response.isSuccessful) {
            throw Exception("Failed to upgrade geo: ${response.code()}")
        }
    }

    suspend fun flushFakeIp(): Result<Unit> = runCatching {
        val response = createApi().flushFakeIp()
        if (!response.isSuccessful) {
            throw Exception("Failed to flush fake IP: ${response.code()}")
        }
    }

    // WebSocket Flows
    suspend fun trafficFlow(): Flow<TrafficData> {
        val config = apiConfigDataStore.currentConfig.first()
        return webSocketManager.trafficFlow(config)
    }

    suspend fun connectionsFlow(): Flow<ConnectionsResponse> {
        val config = apiConfigDataStore.currentConfig.first()
        return webSocketManager.connectionsFlow(config)
    }

    suspend fun logsFlow(level: String = "info"): Flow<LogEntry> {
        val config = apiConfigDataStore.currentConfig.first()
        return webSocketManager.logsFlow(config, level)
    }

    suspend fun memoryFlow(): Flow<MemoryData> {
        val config = apiConfigDataStore.currentConfig.first()
        return webSocketManager.memoryFlow(config)
    }

    // Test connection
    suspend fun testConnection(config: ApiConfig): Result<VersionInfo> = runCatching {
        val client = if (config.secret.isNotEmpty()) {
            okHttpClient.newBuilder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("Authorization", "Bearer ${config.secret}")
                        .build()
                    chain.proceed(request)
                }
                .build()
        } else {
            okHttpClient
        }

        val api = Retrofit.Builder()
            .baseUrl(config.baseUrl.trimEnd('/') + "/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ClashApi::class.java)

        api.getVersion()
    }
}
