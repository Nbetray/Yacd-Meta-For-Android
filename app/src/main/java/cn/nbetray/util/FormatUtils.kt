package cn.nbetray.util

object FormatUtils {

    fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format("%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format("%.1f MB", mb)
        val gb = mb / 1024.0
        return String.format("%.2f GB", gb)
    }

    fun formatSpeed(bytesPerSecond: Long): String {
        return formatBytes(bytesPerSecond) + "/s"
    }

    fun formatLatency(ms: Int): String {
        return when {
            ms <= 0 -> "Timeout"
            ms < 1000 -> "$ms ms"
            else -> String.format("%.1f s", ms / 1000.0)
        }
    }
}
