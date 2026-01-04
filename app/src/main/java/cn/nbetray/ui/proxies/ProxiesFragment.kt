package cn.nbetray.ui.proxies

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
import cn.nbetray.databinding.FragmentProxiesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProxiesFragment : Fragment() {

    private var _binding: FragmentProxiesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProxiesViewModel by viewModels()
    private lateinit var adapter: ProxyGroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProxiesBinding.inflate(inflater, container, false)
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
        adapter = ProxyGroupAdapter(
            onProxyClick = { groupName, proxyName ->
                viewModel.switchProxy(groupName, proxyName)
            },
            onTestLatency = { groupName ->
                viewModel.testGroupLatency(groupName)
            },
            onToggle = { groupName ->
                viewModel.toggleGroup(groupName)
            },
            onLoadMore = { groupName ->
                viewModel.loadMoreProxies(groupName)
            }
        )

        binding.proxyGroupsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ProxiesFragment.adapter
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
            viewModel.loadProxies()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.swipeRefresh.isRefreshing = state.isLoading
                    binding.loading.visibility = if (state.isLoading && state.groups.isEmpty()) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }

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
