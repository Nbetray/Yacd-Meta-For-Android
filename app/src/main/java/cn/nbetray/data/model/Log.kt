package cn.nbetray.data.model

data class LogEntry(
    val type: String,
    val payload: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class LogLevel(val value: String, val displayName: String) {
    DEBUG("debug", "Debug"),
    INFO("info", "Info"),
    WARNING("warning", "Warning"),
    ERROR("error", "Error"),
    SILENT("silent", "Silent");

    companion object {
        fun fromValue(value: String): LogLevel {
            return entries.find { it.value == value } ?: INFO
        }
    }
}
