package cn.nbetray.data.model

import com.google.gson.annotations.SerializedName

data class ProxiesResponse(
    val proxies: Map<String, Proxy>
)

data class Proxy(
    val name: String,
    val type: String,
    val udp: Boolean = false,
    val xudp: Boolean = false,
    val tfo: Boolean = false,
    val history: List<ProxyHistory> = emptyList(),
    val all: List<String>? = null,
    val now: String? = null,
    val hidden: Boolean = false,
    val icon: String? = null
)

data class ProxyHistory(
    val time: String,
    val delay: Int
)

data class ProxyGroupSwitch(
    val name: String
)

data class DelayTestResult(
    val delay: Int
)

data class ProxyProvidersResponse(
    val providers: Map<String, ProxyProvider>
)

data class ProxyProvider(
    val name: String,
    val type: String,
    val vehicleType: String,
    val proxies: List<Proxy>? = null,
    val updatedAt: String? = null,
    @SerializedName("subscriptionInfo")
    val subscriptionInfo: SubscriptionInfo? = null
)

data class SubscriptionInfo(
    val upload: Long = 0,
    val download: Long = 0,
    val total: Long = 0,
    val expire: Long = 0
)
