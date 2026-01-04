package cn.nbetray.ui.proxies

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cn.nbetray.databinding.ItemProxyDotBinding

class ProxyDotAdapter(
    private val onProxyClick: (proxyName: String) -> Unit
) : ListAdapter<ProxyItem, ProxyDotAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProxyDotBinding.inflate(
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
        private val binding: ItemProxyDotBinding
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
            val color = when {
                proxy.isTesting -> Color.parseColor("#909399") // Gray for testing
                proxy.delay <= 0 -> Color.parseColor("#909399") // Gray for unknown
                proxy.delay < 200 -> Color.parseColor("#22C55E") // Green for fast
                proxy.delay < 500 -> Color.parseColor("#F59E0B") // Yellow for medium
                else -> Color.parseColor("#EF4444") // Red for slow
            }

            val drawable = binding.proxyDot.background as? GradientDrawable
            drawable?.setColor(color)

            // Show selected indicator
            binding.selectedIndicator.visibility = if (proxy.isSelected) View.VISIBLE else View.GONE

            // Scale up selected item
            val scale = if (proxy.isSelected) 1.3f else 1f
            binding.root.scaleX = scale
            binding.root.scaleY = scale
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
