package cn.nbetray.ui.logs

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cn.nbetray.data.model.LogEntry
import cn.nbetray.databinding.ItemLogBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class LogsAdapter : ListAdapter<LogEntry, LogsAdapter.ViewHolder>(DiffCallback()) {

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(log: LogEntry) {
            binding.logTime.text = timeFormat.format(Date(log.timestamp))
            binding.logMessage.text = log.payload

            val (levelText, levelColor) = when (log.type.lowercase()) {
                "debug" -> "[DBG]" to Color.GRAY
                "info" -> "[INF]" to Color.parseColor("#22C55E")
                "warning" -> "[WRN]" to Color.parseColor("#F59E0B")
                "error" -> "[ERR]" to Color.parseColor("#EF4444")
                else -> "[${log.type.uppercase().take(3)}]" to Color.GRAY
            }

            binding.logLevel.text = levelText
            binding.logLevel.setTextColor(levelColor)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<LogEntry>() {
        override fun areItemsTheSame(oldItem: LogEntry, newItem: LogEntry): Boolean {
            return oldItem.timestamp == newItem.timestamp && oldItem.payload == newItem.payload
        }

        override fun areContentsTheSame(oldItem: LogEntry, newItem: LogEntry): Boolean {
            return oldItem == newItem
        }
    }
}
