package cn.nbetray.ui.connections

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cn.nbetray.data.model.Connection
import cn.nbetray.databinding.ItemConnectionBinding
import cn.nbetray.util.FormatUtils

class ConnectionsAdapter(
    private val onCloseClick: (connectionId: String) -> Unit
) : ListAdapter<Connection, ConnectionsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConnectionBinding.inflate(
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
        private val binding: ItemConnectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(connection: Connection) {
            val metadata = connection.metadata
            val host = metadata.host.ifEmpty {
                "${metadata.destinationIP}:${metadata.destinationPort}"
            }

            binding.host.text = host
            binding.rule.text = "${connection.rule} → ${connection.rulePayload}"
            binding.chains.text = connection.chains.joinToString(" → ")
            binding.process.text = metadata.process.ifEmpty { "Unknown" }
            binding.network.text = metadata.network.uppercase()
            binding.upload.text = "↑ ${FormatUtils.formatBytes(connection.upload)}"
            binding.download.text = "↓ ${FormatUtils.formatBytes(connection.download)}"

            binding.btnClose.setOnClickListener {
                onCloseClick(connection.id)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Connection>() {
        override fun areItemsTheSame(oldItem: Connection, newItem: Connection): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Connection, newItem: Connection): Boolean {
            return oldItem == newItem
        }
    }
}
