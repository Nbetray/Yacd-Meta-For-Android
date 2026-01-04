package cn.nbetray.data.model

import com.google.gson.annotations.SerializedName

data class ClashConfig(
    val port: Int = 0,
    @SerializedName("socks-port")
    val socksPort: Int = 0,
    @SerializedName("redir-port")
    val redirPort: Int = 0,
    @SerializedName("tproxy-port")
    val tproxyPort: Int = 0,
    @SerializedName("mixed-port")
    val mixedPort: Int = 0,
    @SerializedName("allow-lan")
    val allowLan: Boolean = false,
    @SerializedName("bind-address")
    val bindAddress: String = "*",
    val mode: String = "rule",
    @SerializedName("log-level")
    val logLevel: String = "info",
    val ipv6: Boolean = false,
    val tun: TunConfig? = null
)

data class TunConfig(
    val enable: Boolean = false,
    val device: String = "",
    val stack: String = "gvisor",
    @SerializedName("dns-hijack")
    val dnsHijack: List<String> = emptyList(),
    @SerializedName("auto-route")
    val autoRoute: Boolean = true,
    @SerializedName("auto-detect-interface")
    val autoDetectInterface: Boolean = true
)

data class ConfigPatch(
    val port: Int? = null,
    @SerializedName("socks-port")
    val socksPort: Int? = null,
    @SerializedName("mixed-port")
    val mixedPort: Int? = null,
    @SerializedName("allow-lan")
    val allowLan: Boolean? = null,
    val mode: String? = null,
    @SerializedName("log-level")
    val logLevel: String? = null,
    val tun: TunConfig? = null
)
