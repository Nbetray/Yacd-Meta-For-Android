package cn.nbetray.ui.rules

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cn.nbetray.data.model.Rule
import cn.nbetray.databinding.ItemRuleSimpleBinding

class RuleSimpleAdapter : ListAdapter<Rule, RuleSimpleAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRuleSimpleBinding.inflate(
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
        private val binding: ItemRuleSimpleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(rule: Rule) {
            binding.ruleType.text = rule.type
            binding.rulePayload.text = rule.payload
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
