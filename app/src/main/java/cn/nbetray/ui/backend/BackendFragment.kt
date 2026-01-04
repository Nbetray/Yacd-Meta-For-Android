package cn.nbetray.ui.backend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import cn.nbetray.R
import cn.nbetray.databinding.FragmentBackendBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BackendFragment : Fragment() {

    private var _binding: FragmentBackendBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BackendViewModel by viewModels()
    private lateinit var adapter: BackendAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBackendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupButtons()
        observeState()
    }

    private fun setupRecyclerView() {
        adapter = BackendAdapter(
            onItemClick = { config ->
                binding.baseUrl.setText(config.baseUrl)
                binding.secret.setText(config.secret)
            },
            onDeleteClick = { config ->
                viewModel.removeConfig(config)
            }
        )

        binding.savedBackendsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@BackendFragment.adapter
        }
    }

    private fun setupButtons() {
        binding.btnTest.setOnClickListener {
            val baseUrl = binding.baseUrl.text?.toString() ?: ""
            val secret = binding.secret.text?.toString() ?: ""
            viewModel.testConnection(baseUrl, secret)
        }

        binding.btnConnect.setOnClickListener {
            val baseUrl = binding.baseUrl.text?.toString() ?: ""
            val secret = binding.secret.text?.toString() ?: ""
            viewModel.connect(baseUrl, secret)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        // Update current config fields
                        if (binding.baseUrl.text.isNullOrEmpty()) {
                            binding.baseUrl.setText(state.currentConfig.baseUrl)
                            binding.secret.setText(state.currentConfig.secret)
                        }

                        // Show status card if connected
                        if (state.versionInfo != null) {
                            binding.statusCard.visibility = View.VISIBLE
                            val versionText = if (state.versionInfo.meta) {
                                "Connected to Clash Meta ${state.versionInfo.version}"
                            } else {
                                "Connected to Clash ${state.versionInfo.version}"
                            }
                            binding.statusText.text = versionText
                        } else {
                            binding.statusCard.visibility = View.GONE
                        }

                        // Show messages
                        state.message?.let { message ->
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                            viewModel.clearMessage()
                        }
                    }
                }

                launch {
                    viewModel.savedConfigs.collect { configs ->
                        adapter.submitList(configs)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
