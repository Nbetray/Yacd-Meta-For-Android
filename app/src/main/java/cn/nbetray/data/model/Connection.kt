package cn.nbetray.data.model

import com.google.gson.annotations.SerializedName

data class ConnectionsResponse(
    val downloadTotal: Long,
    val uploadTotal: Long,
    val connections: List<Connection>?,
    val memory: Long = 0
)

data class Connection(
    val id: String,
    val metadata: ConnectionMetadata,
    val upload: Long,
    val download: Long,
    val start: String,
    val chains: List<String>,
    val rule: String,
    val rulePayload: String
)

data class ConnectionMetadata(
    val network: String,
    val type: String,
    val sourceIP: String,
    val destinationIP: String,
    val sourcePort: String,
    val destinationPort: String,
    val host: String,
    val dnsMode: String,
    val uid: Int = 0,
    val process: String = "",
    val processPath: String = "",
    val specialProxy: String = "",
    val specialRules: String = "",
    val remoteDestination: String = "",
    val sniffHost: String = ""
)

data class TrafficData(
    val up: Long,
    val down: Long
)

data class MemoryData(
    val inuse: Long,
    val oslimit: Long = 0
)
