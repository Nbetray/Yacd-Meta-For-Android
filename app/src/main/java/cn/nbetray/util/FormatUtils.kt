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

    /**
     * Format bytes for chart Y-axis (KB or MB, max unit is MB)
     */
    fun formatChartBytes(bytes: Float): String {
        val kb = bytes / 1024f
        return if (kb >= 1024f) {
            String.format("%.1fMB", kb / 1024f)
        } else {
            String.format("%.0fKB", kb)
        }
    }
}
