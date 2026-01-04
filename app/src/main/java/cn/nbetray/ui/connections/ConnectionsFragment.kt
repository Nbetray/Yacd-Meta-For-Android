package cn.nbetray.ui.connections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import cn.nbetray.R
import cn.nbetray.databinding.FragmentConnectionsBinding
import cn.nbetray.util.FormatUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConnectionsFragment : Fragment() {

    private var _binding: FragmentConnectionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConnectionsViewModel by viewModels()
    private lateinit var adapter: ConnectionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupButtons()
        observeState()
    }

    private fun setupRecyclerView() {
        adapter = ConnectionsAdapter { connectionId ->
            viewModel.closeConnection(connectionId)
        }
        binding.connectionsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ConnectionsFragment.adapter
        }
    }

    private fun setupButtons() {
        binding.btnPause.setOnClickListener {
            viewModel.togglePause()
        }

        binding.btnCloseAll.setOnClickListener {
            viewModel.closeAllConnections()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.loading.visibility = if (state.isLoading) View.VISIBLE else View.GONE

                    // Update stats
                    binding.activeCount.text = getString(
                        R.string.active_connections,
                        state.connections.size
                    )
                    binding.uploadTotal.text = "↑ ${FormatUtils.formatBytes(state.uploadTotal)}"
                    binding.downloadTotal.text = "↓ ${FormatUtils.formatBytes(state.downloadTotal)}"

                    // Update pause button text
                    binding.btnPause.text = if (state.isPaused) {
                        getString(R.string.resume)
                    } else {
                        getString(R.string.pause)
                    }

                    adapter.submitList(state.connections)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
