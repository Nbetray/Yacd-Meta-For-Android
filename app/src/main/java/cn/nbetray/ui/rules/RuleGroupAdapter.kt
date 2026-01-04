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
    private val onToggleClick: (proxy: String) -> Unit
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
            binding.ruleCount.text = binding.root.context.getString(
                R.string.rule_count,
                group.rules.size
            )

            // Handle expand/collapse state
            if (group.isExpanded) {
                binding.rulesList.visibility = View.VISIBLE
                binding.btnToggle.setImageResource(R.drawable.ic_expand_less)
                rulesAdapter.submitList(group.rules)
            } else {
                binding.rulesList.visibility = View.GONE
                binding.btnToggle.setImageResource(R.drawable.ic_expand_more)
            }

            // Toggle click handlers
            val toggleAction = { onToggleClick(group.proxy) }
            binding.btnToggle.setOnClickListener { toggleAction() }
            binding.header.setOnClickListener { toggleAction() }
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
