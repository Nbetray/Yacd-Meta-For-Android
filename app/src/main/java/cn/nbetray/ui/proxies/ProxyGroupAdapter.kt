package cn.nbetray.ui.proxies

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cn.nbetray.R
import cn.nbetray.databinding.ItemProxyGroupBinding

class ProxyGroupAdapter(
    private val onProxyClick: (groupName: String, proxyName: String) -> Unit,
    private val onTestLatency: (groupName: String) -> Unit,
    private val onToggle: (groupName: String) -> Unit,
    private val onLoadMore: (groupName: String) -> Unit
) : ListAdapter<ProxyGroup, ProxyGroupAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProxyGroupBinding.inflate(
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
        private val binding: ItemProxyGroupBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val proxyAdapter = ProxyAdapter { proxyName ->
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onProxyClick(getItem(position).name, proxyName)
            }
        }

        private val proxyDotAdapter = ProxyDotAdapter { proxyName ->
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onProxyClick(getItem(position).name, proxyName)
            }
        }

        init {
            binding.proxiesList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = proxyAdapter
                setHasFixedSize(true)
                itemAnimator = null
                isNestedScrollingEnabled = false
            }

            binding.proxiesSummary.apply {
                layoutManager = GridLayoutManager(context, 12)
                adapter = proxyDotAdapter
                setHasFixedSize(true)
                itemAnimator = null
                isNestedScrollingEnabled = false
            }
        }

        fun bind(group: ProxyGroup) {
            binding.groupName.text = group.name
            binding.groupType.text = group.type.lowercase()
            binding.currentProxy.text = group.now ?: "None"

            val totalSize = group.proxies.size

            binding.proxyCount.text = binding.root.context.getString(
                R.string.proxy_count,
                totalSize
            )

            // Handle testing state
            if (group.isTesting) {
                binding.testingProgress.visibility = View.VISIBLE
                binding.btnTestLatency.visibility = View.GONE
            } else {
                binding.testingProgress.visibility = View.GONE
                binding.btnTestLatency.visibility = View.VISIBLE
            }

            // Handle expand/collapse state
            if (group.isExpanded) {
                binding.btnToggle.setImageResource(R.drawable.ic_expand_less)
                binding.proxiesList.visibility = View.VISIBLE
                binding.proxiesSummary.visibility = View.GONE

                // Use displayedProxies from data model
                proxyAdapter.submitList(group.displayedProxies)

                // Show/hide load more button
                if (group.hasMore) {
                    binding.btnLoadMore.visibility = View.VISIBLE
                    binding.btnLoadMore.text = binding.root.context.getString(
                        R.string.load_more,
                        group.displayedCount,
                        totalSize
                    )
                } else {
                    binding.btnLoadMore.visibility = View.GONE
                }
            } else {
                binding.btnToggle.setImageResource(R.drawable.ic_expand_more)
                binding.proxiesList.visibility = View.GONE
                binding.proxiesSummary.visibility = View.VISIBLE
                binding.btnLoadMore.visibility = View.GONE
            }

            // Toggle click handlers
            binding.btnToggle.setOnClickListener { onToggle(group.name) }
            binding.header.setOnClickListener { onToggle(group.name) }

            binding.btnTestLatency.setOnClickListener {
                onTestLatency(group.name)
            }

            // Load more button
            binding.btnLoadMore.setOnClickListener {
                onLoadMore(group.name)
            }

            // Dot view: always show all (dots are lightweight)
            proxyDotAdapter.submitList(group.proxies)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ProxyGroup>() {
        override fun areItemsTheSame(oldItem: ProxyGroup, newItem: ProxyGroup): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: ProxyGroup, newItem: ProxyGroup): Boolean {
            return oldItem == newItem
        }
    }
}
