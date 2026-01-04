package cn.nbetray.data.api

import cn.nbetray.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ClashApi {

    // Version
    @GET("version")
    suspend fun getVersion(): VersionInfo

    // Configs
    @GET("configs")
    suspend fun getConfigs(): ClashConfig

    @PATCH("configs")
    suspend fun patchConfigs(@Body config: ConfigPatch): Response<Unit>

    @PUT("configs")
    suspend fun reloadConfigs(@Query("force") force: Boolean = true): Response<Unit>

    // Proxies
    @GET("proxies")
    suspend fun getProxies(): ProxiesResponse

    @PUT("proxies/{name}")
    suspend fun switchProxy(
        @Path("name") groupName: String,
        @Body proxy: ProxyGroupSwitch
    ): Response<Unit>

    @GET("proxies/{name}/delay")
    suspend fun testProxyDelay(
        @Path("name") proxyName: String,
        @Query("timeout") timeout: Int = 5000,
        @Query("url") url: String = "https://www.gstatic.com/generate_204"
    ): DelayTestResult

    @GET("group/{name}/delay")
    suspend fun testGroupDelay(
        @Path("name") groupName: String,
        @Query("timeout") timeout: Int = 5000,
        @Query("url") url: String = "https://www.gstatic.com/generate_204"
    ): Map<String, Int>

    // Proxy Providers
    @GET("providers/proxies")
    suspend fun getProxyProviders(): ProxyProvidersResponse

    @PUT("providers/proxies/{name}")
    suspend fun updateProxyProvider(@Path("name") providerName: String): Response<Unit>

    @GET("providers/proxies/{name}/healthcheck")
    suspend fun healthCheckProxyProvider(@Path("name") providerName: String): Response<Unit>

    // Rules
    @GET("rules")
    suspend fun getRules(): RulesResponse

    // Rule Providers
    @GET("providers/rules")
    suspend fun getRuleProviders(): RuleProvidersResponse

    @PUT("providers/rules/{name}")
    suspend fun updateRuleProvider(@Path("name") providerName: String): Response<Unit>

    // Connections
    @GET("connections")
    suspend fun getConnections(): ConnectionsResponse

    @DELETE("connections")
    suspend fun closeAllConnections(): Response<Unit>

    @DELETE("connections/{id}")
    suspend fun closeConnection(@Path("id") connectionId: String): Response<Unit>

    // Control
    @POST("restart")
    suspend fun restart(): Response<Unit>

    @POST("upgrade")
    suspend fun upgrade(): Response<Unit>

    @POST("upgrade/geo")
    suspend fun upgradeGeo(): Response<Unit>

    @POST("cache/fakeip/flush")
    suspend fun flushFakeIp(): Response<Unit>
}
