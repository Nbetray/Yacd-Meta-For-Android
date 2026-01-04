package cn.nbetray.ui.backend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cn.nbetray.data.local.ApiConfig
import cn.nbetray.databinding.ItemBackendBinding

class BackendAdapter(
    private val onItemClick: (ApiConfig) -> Unit,
    private val onDeleteClick: (ApiConfig) -> Unit
) : ListAdapter<ApiConfig, BackendAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBackendBinding.inflate(
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
        private val binding: ItemBackendBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(config: ApiConfig) {
            binding.backendUrl.text = config.displayName

            if (config.secret.isNotEmpty()) {
                binding.backendSecret.visibility = View.VISIBLE
                binding.backendSecret.text = "Secret: ****"
            } else {
                binding.backendSecret.visibility = View.GONE
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(config)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ApiConfig>() {
        override fun areItemsTheSame(oldItem: ApiConfig, newItem: ApiConfig): Boolean {
            return oldItem.baseUrl == newItem.baseUrl
        }

        override fun areContentsTheSame(oldItem: ApiConfig, newItem: ApiConfig): Boolean {
            return oldItem == newItem
        }
    }
}
