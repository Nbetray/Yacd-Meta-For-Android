package cn.nbetray.ui.proxies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.nbetray.data.local.SettingsDataStore
import cn.nbetray.data.model.Proxy
import cn.nbetray.data.repository.ClashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProxyGroup(
    val name: String,
    val type: String,
    val now: String?,
    val all: List<String>,
    val proxies: List<ProxyItem>,
    val isTesting: Boolean = false,
    val isExpanded: Boolean = false
)

data class ProxyItem(
    val name: String,
    val type: String,
    val delay: Int = 0,
    val isSelected: Boolean = false,
    val isTesting: Boolean = false
)

data class ProxiesUiState(
    val groups: List<ProxyGroup> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val filterText: String = ""
)

@HiltViewModel
class ProxiesViewModel @Inject constructor(
    private val repository: ClashRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProxiesUiState())
    val uiState: StateFlow<ProxiesUiState> = _uiState

    private val _delays = MutableStateFlow<Map<String, Int>>(emptyMap())
    val delays: StateFlow<Map<String, Int>> = _delays

    private var allProxies: Map<String, Proxy> = emptyMap()

    init {
        loadProxies()
    }

    fun loadProxies() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getProxies()
                .onSuccess { response ->
                    allProxies = response.proxies
                    val groups = parseProxyGroups(response.proxies)
                    _uiState.value = _uiState.value.copy(
                        groups = groups,
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
        }
    }

    private fun parseProxyGroups(proxies: Map<String, Proxy>): List<ProxyGroup> {
        val groupTypes = setOf("Selector", "URLTest", "Fallback", "LoadBalance", "Relay")

        return proxies.values
            .filter { it.type in groupTypes && it.all != null }
            .map { proxy ->
                val proxyItems = proxy.all!!.mapNotNull { proxyName ->
                    proxies[proxyName]?.let { p ->
                        ProxyItem(
                            name = p.name,
                            type = p.type,
                            delay = _delays.value[p.name] ?: p.history.lastOrNull()?.delay ?: 0,
                            isSelected = proxy.now == p.name
                        )
                    }
                }

                ProxyGroup(
                    name = proxy.name,
                    type = proxy.type,
                    now = proxy.now,
                    all = proxy.all,
                    proxies = proxyItems
                )
            }
            .sortedBy { it.name }
    }

    fun switchProxy(groupName: String, proxyName: String) {
        viewModelScope.launch {
            repository.switchProxy(groupName, proxyName)
                .onSuccess {
                    loadProxies()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
        }
    }

    fun testGroupLatency(groupName: String) {
        viewModelScope.launch {
            // Set testing state for the group and its proxies
            val testingGroups = _uiState.value.groups.map { group ->
                if (group.name == groupName) {
                    group.copy(
                        isTesting = true,
                        proxies = group.proxies.map { proxy ->
                            proxy.copy(isTesting = true)
                        }
                    )
                } else {
                    group
                }
            }
            _uiState.value = _uiState.value.copy(groups = testingGroups)

            val url = settingsDataStore.latencyTestUrl.first()
            repository.testGroupDelay(groupName, url)
                .onSuccess { delays ->
                    val currentDelays = _delays.value.toMutableMap()
                    currentDelays.putAll(delays)
                    _delays.value = currentDelays

                    // Update UI state with new delays and clear testing state
                    val updatedGroups = _uiState.value.groups.map { group ->
                        if (group.name == groupName) {
                            group.copy(
                                isTesting = false,
                                proxies = group.proxies.map { proxy ->
                                    proxy.copy(
                                        delay = delays[proxy.name] ?: proxy.delay,
                                        isTesting = false
                                    )
                                }
                            )
                        } else {
                            group
                        }
                    }
                    _uiState.value = _uiState.value.copy(groups = updatedGroups)
                }
                .onFailure { e ->
                    // Clear testing state on failure
                    val updatedGroups = _uiState.value.groups.map { group ->
                        if (group.name == groupName) {
                            group.copy(
                                isTesting = false,
                                proxies = group.proxies.map { proxy ->
                                    proxy.copy(isTesting = false)
                                }
                            )
                        } else {
                            group
                        }
                    }
                    _uiState.value = _uiState.value.copy(groups = updatedGroups, error = e.message)
                }
        }
    }

    fun setFilterText(text: String) {
        _uiState.value = _uiState.value.copy(filterText = text)
    }

    fun toggleGroup(groupName: String) {
        val updatedGroups = _uiState.value.groups.map { group ->
            if (group.name == groupName) {
                group.copy(isExpanded = !group.isExpanded)
            } else {
                group
            }
        }
        _uiState.value = _uiState.value.copy(groups = updatedGroups)
    }

    fun getFilteredGroups(): List<ProxyGroup> {
        val filter = _uiState.value.filterText.lowercase()
        if (filter.isEmpty()) return _uiState.value.groups

        return _uiState.value.groups.map { group ->
            group.copy(
                proxies = group.proxies.filter {
                    it.name.lowercase().contains(filter)
                }
            )
        }.filter { it.proxies.isNotEmpty() || it.name.lowercase().contains(filter) }
    }
}
