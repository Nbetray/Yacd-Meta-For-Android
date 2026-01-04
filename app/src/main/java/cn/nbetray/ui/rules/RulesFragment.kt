package cn.nbetray.ui.rules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import cn.nbetray.R
import cn.nbetray.databinding.FragmentRulesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RulesFragment : Fragment() {

    private var _binding: FragmentRulesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RulesViewModel by viewModels()
    private lateinit var adapter: RuleGroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRulesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupSwipeRefresh()
        observeState()
    }

    private fun setupRecyclerView() {
        adapter = RuleGroupAdapter { proxy ->
            viewModel.toggleGroup(proxy)
        }
        binding.rulesList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RulesFragment.adapter
            setHasFixedSize(true)
            itemAnimator = null // Disable animations for smoother scrolling
            setItemViewCacheSize(10)
        }
    }

    private fun setupSearch() {
        binding.searchInput.doAfterTextChanged { text ->
            viewModel.setFilterText(text?.toString() ?: "")
            updateList()
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadRules()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.swipeRefresh.isRefreshing = state.isLoading
                    binding.totalRules.text = getString(R.string.total_rules, state.totalRules)
                    updateList()
                }
            }
        }
    }

    private fun updateList() {
        adapter.submitList(viewModel.getFilteredGroups())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
