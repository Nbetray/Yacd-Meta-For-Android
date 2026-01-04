package cn.nbetray.ui.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.nbetray.data.model.Rule
import cn.nbetray.data.repository.ClashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RuleGroup(
    val proxy: String,
    val rules: List<Rule>,
    val isExpanded: Boolean = false,
    val displayedCount: Int = PAGE_SIZE
) {
    companion object {
        const val PAGE_SIZE = 50
    }

    val displayedRules: List<Rule>
        get() = rules.take(displayedCount)

    val hasMore: Boolean
        get() = displayedCount < rules.size
}

data class RulesUiState(
    val ruleGroups: List<RuleGroup> = emptyList(),
    val totalRules: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val filterText: String = ""
)

@HiltViewModel
class RulesViewModel @Inject constructor(
    private val repository: ClashRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RulesUiState())
    val uiState: StateFlow<RulesUiState> = _uiState

    init {
        loadRules()
    }

    fun loadRules() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getRules()
                .onSuccess { response ->
                    val groups = response.rules
                        .groupBy { it.proxy }
                        .map { (proxy, rules) ->
                            RuleGroup(
                                proxy = proxy,
                                rules = rules
                            )
                        }
                        .sortedByDescending { it.rules.size }

                    _uiState.value = _uiState.value.copy(
                        ruleGroups = groups,
                        totalRules = response.rules.size,
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

    fun toggleGroup(proxy: String) {
        val updatedGroups = _uiState.value.ruleGroups.map { group ->
            if (group.proxy == proxy) {
                // Reset displayedCount when collapsing
                val newDisplayedCount = if (group.isExpanded) RuleGroup.PAGE_SIZE else group.displayedCount
                group.copy(isExpanded = !group.isExpanded, displayedCount = newDisplayedCount)
            } else {
                group
            }
        }
        _uiState.value = _uiState.value.copy(ruleGroups = updatedGroups)
    }

    fun loadMoreRules(proxy: String) {
        val updatedGroups = _uiState.value.ruleGroups.map { group ->
            if (group.proxy == proxy) {
                val newCount = minOf(group.displayedCount + RuleGroup.PAGE_SIZE, group.rules.size)
                group.copy(displayedCount = newCount)
            } else {
                group
            }
        }
        _uiState.value = _uiState.value.copy(ruleGroups = updatedGroups)
    }

    fun setFilterText(text: String) {
        _uiState.value = _uiState.value.copy(filterText = text)
    }

    fun getFilteredGroups(): List<RuleGroup> {
        val filter = _uiState.value.filterText.lowercase()
        if (filter.isEmpty()) return _uiState.value.ruleGroups

        return _uiState.value.ruleGroups.mapNotNull { group ->
            val filteredRules = group.rules.filter { rule ->
                rule.payload.lowercase().contains(filter) ||
                rule.type.lowercase().contains(filter) ||
                rule.proxy.lowercase().contains(filter)
            }
            if (filteredRules.isNotEmpty() || group.proxy.lowercase().contains(filter)) {
                group.copy(rules = if (filteredRules.isEmpty()) group.rules else filteredRules)
            } else {
                null
            }
        }
    }
}
