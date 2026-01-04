package cn.nbetray.ui.proxies

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cn.nbetray.databinding.ItemProxyBinding
import cn.nbetray.util.FormatUtils

class ProxyAdapter(
    private val onProxyClick: (proxyName: String) -> Unit
) : ListAdapter<ProxyItem, ProxyAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProxyBinding.inflate(
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
        private val binding: ItemProxyBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onProxyClick(getItem(position).name)
                }
            }
        }

        fun bind(proxy: ProxyItem) {
            binding.proxyName.text = proxy.name
            binding.proxyType.text = proxy.type

            // Handle testing state
            if (proxy.isTesting) {
                binding.latencyLoading.visibility = View.VISIBLE
                binding.proxyLatency.visibility = View.GONE
            } else {
                binding.latencyLoading.visibility = View.GONE
                binding.proxyLatency.visibility = View.VISIBLE

                // Set latency with color coding
                if (proxy.delay > 0) {
                    binding.proxyLatency.text = FormatUtils.formatLatency(proxy.delay)
                    binding.proxyLatency.setTextColor(
                        when {
                            proxy.delay < 200 -> Color.parseColor("#22C55E") // Green
                            proxy.delay < 500 -> Color.parseColor("#F59E0B") // Yellow
                            else -> Color.parseColor("#EF4444") // Red
                        }
                    )
                } else {
                    binding.proxyLatency.text = ""
                }
            }

            // Highlight selected proxy
            binding.proxyCard.isChecked = proxy.isSelected
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ProxyItem>() {
        override fun areItemsTheSame(oldItem: ProxyItem, newItem: ProxyItem): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: ProxyItem, newItem: ProxyItem): Boolean {
            return oldItem == newItem
        }
    }
}
