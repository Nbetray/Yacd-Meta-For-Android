package cn.nbetray.ui.rules

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cn.nbetray.R
import cn.nbetray.databinding.ItemRuleGroupBinding

class RuleGroupAdapter(
    private val onToggleClick: (proxy: String) -> Unit,
    private val onLoadMore: (proxy: String) -> Unit
) : ListAdapter<RuleGroup, RuleGroupAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRuleGroupBinding.inflate(
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
        private val binding: ItemRuleGroupBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val rulesAdapter = RuleSimpleAdapter()

        init {
            binding.rulesList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = rulesAdapter
                setHasFixedSize(true)
                itemAnimator = null
                isNestedScrollingEnabled = false
            }
        }

        fun bind(group: RuleGroup) {
            binding.proxyName.text = group.proxy
            val totalSize = group.rules.size
            binding.ruleCount.text = binding.root.context.getString(
                R.string.rule_count,
                totalSize
            )

            // Handle expand/collapse state
            if (group.isExpanded) {
                binding.rulesList.visibility = View.VISIBLE
                binding.btnToggle.setImageResource(R.drawable.ic_expand_less)

                // Use displayedRules from data model
                rulesAdapter.submitList(group.displayedRules)

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
                binding.rulesList.visibility = View.GONE
                binding.btnToggle.setImageResource(R.drawable.ic_expand_more)
                binding.btnLoadMore.visibility = View.GONE
            }

            // Toggle click handlers
            binding.btnToggle.setOnClickListener { onToggleClick(group.proxy) }
            binding.header.setOnClickListener { onToggleClick(group.proxy) }

            // Load more button
            binding.btnLoadMore.setOnClickListener {
                onLoadMore(group.proxy)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<RuleGroup>() {
        override fun areItemsTheSame(oldItem: RuleGroup, newItem: RuleGroup): Boolean {
            return oldItem.proxy == newItem.proxy
        }

        override fun areContentsTheSame(oldItem: RuleGroup, newItem: RuleGroup): Boolean {
            return oldItem == newItem
        }
    }
}
