package cn.nbetray.ui.rules

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cn.nbetray.data.model.Rule
import cn.nbetray.databinding.ItemRuleBinding

class RulesAdapter : ListAdapter<Rule, RulesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRuleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemRuleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(rule: Rule) {
            binding.rulePayload.text = rule.payload
            binding.ruleType.text = rule.type
            binding.ruleProxy.text = rule.proxy
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Rule>() {
        override fun areItemsTheSame(oldItem: Rule, newItem: Rule): Boolean {
            return oldItem.payload == newItem.payload && oldItem.type == newItem.type
        }

        override fun areContentsTheSame(oldItem: Rule, newItem: Rule): Boolean {
            return oldItem == newItem
        }
    }
}
