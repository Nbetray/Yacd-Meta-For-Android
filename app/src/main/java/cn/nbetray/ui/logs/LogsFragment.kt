package cn.nbetray.ui.logs

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
import cn.nbetray.databinding.FragmentLogsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LogsFragment : Fragment() {

    private var _binding: FragmentLogsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LogsViewModel by viewModels()
    private lateinit var adapter: LogsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupLogLevelChips()
        setupButtons()
        observeState()
    }

    private fun setupRecyclerView() {
        adapter = LogsAdapter()
        binding.logsList.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = this@LogsFragment.adapter
        }
    }

    private fun setupLogLevelChips() {
        binding.logLevelChips.setOnCheckedStateChangeListener { _, checkedIds ->
            val level = when (checkedIds.firstOrNull()) {
                R.id.chip_debug -> "debug"
                R.id.chip_info -> "info"
                R.id.chip_warning -> "warning"
                R.id.chip_error -> "error"
                else -> "info"
            }
            viewModel.setLogLevel(level)
        }
    }

    private fun setupButtons() {
        binding.btnClear.setOnClickListener {
            viewModel.clearLogs()
        }

        binding.fabPause.setOnClickListener {
            viewModel.togglePause()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.logs) {
                        // Scroll to bottom when new log arrives
                        if (state.logs.isNotEmpty() && !state.isPaused) {
                            binding.logsList.scrollToPosition(state.logs.size - 1)
                        }
                    }

                    // Update FAB icon based on pause state
                    binding.fabPause.setImageResource(
                        if (state.isPaused) {
                            android.R.drawable.ic_media_play
                        } else {
                            android.R.drawable.ic_media_pause
                        }
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
