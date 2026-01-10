package cn.nbetray.ui.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.nbetray.data.local.SettingsDataStore
import cn.nbetray.data.model.ClashConfig
import cn.nbetray.data.model.ConfigPatch
import cn.nbetray.data.repository.ClashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConfigUiState(
    val config: ClashConfig? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val language: String = SettingsDataStore.LANG_SYSTEM
)

@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val repository: ClashRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigUiState())
    val uiState: StateFlow<ConfigUiState> = _uiState

    init {
        loadConfig()
        loadLanguage()
    }

    private fun loadLanguage() {
        viewModelScope.launch {
            val lang = settingsDataStore.language.first()
            _uiState.value = _uiState.value.copy(language = lang)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            settingsDataStore.setLanguage(lang)
            _uiState.value = _uiState.value.copy(language = lang)
        }
    }

    fun loadConfig() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getConfigs()
                .onSuccess { config ->
                    _uiState.value = _uiState.value.copy(
                        config = config,
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

    fun updateMode(mode: String) {
        viewModelScope.launch {
            // Optimistic update: update UI immediately for better responsiveness
            val previousConfig = _uiState.value.config
            _uiState.value = _uiState.value.copy(
                config = previousConfig?.copy(mode = mode)
            )

            repository.patchConfigs(ConfigPatch(mode = mode))
                .onSuccess {
                    // Keep the optimistic update, no need to reload
                }
                .onFailure { e ->
                    // Rollback to previous state on failure
                    _uiState.value = _uiState.value.copy(
                        config = previousConfig,
                        message = "Failed to update mode: ${e.message}"
                    )
                }
        }
    }

    fun updateAllowLan(allow: Boolean) {
        viewModelScope.launch {
            // Optimistic update: update UI immediately for better responsiveness
            val previousConfig = _uiState.value.config
            _uiState.value = _uiState.value.copy(
                config = previousConfig?.copy(allowLan = allow)
            )

            repository.patchConfigs(ConfigPatch(allowLan = allow))
                .onSuccess {
                    // Keep the optimistic update, no need to reload
                }
                .onFailure { e ->
                    // Rollback to previous state on failure
                    _uiState.value = _uiState.value.copy(
                        config = previousConfig,
                        message = "Failed to update allow LAN: ${e.message}"
                    )
                }
        }
    }

    fun reloadConfig() {
        viewModelScope.launch {
            repository.reloadConfigs()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(message = "Config reloaded")
                    loadConfig()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(message = "Failed: ${e.message}")
                }
        }
    }

    fun restart() {
        viewModelScope.launch {
            repository.restart()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(message = "Core restarted")
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(message = "Failed: ${e.message}")
                }
        }
    }

    fun upgradeGeo() {
        viewModelScope.launch {
            repository.upgradeGeo()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(message = "GeoIP updated")
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(message = "Failed: ${e.message}")
                }
        }
    }

    fun flushFakeIp() {
        viewModelScope.launch {
            repository.flushFakeIp()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(message = "FakeIP flushed")
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(message = "Failed: ${e.message}")
                }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
