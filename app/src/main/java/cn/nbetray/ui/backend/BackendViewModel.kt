package cn.nbetray.ui.backend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.nbetray.data.local.ApiConfig
import cn.nbetray.data.local.ApiConfigDataStore
import cn.nbetray.data.model.VersionInfo
import cn.nbetray.data.repository.ClashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackendUiState(
    val currentConfig: ApiConfig = ApiConfig(),
    val versionInfo: VersionInfo? = null,
    val isLoading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class BackendViewModel @Inject constructor(
    private val repository: ClashRepository,
    private val apiConfigDataStore: ApiConfigDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackendUiState())
    val uiState: StateFlow<BackendUiState> = _uiState

    val savedConfigs = apiConfigDataStore.savedConfigs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            apiConfigDataStore.currentConfig.collect { config ->
                _uiState.value = _uiState.value.copy(currentConfig = config)
                // Try to connect to current config
                testCurrentConnection()
            }
        }
    }

    private fun testCurrentConnection() {
        viewModelScope.launch {
            repository.getVersion()
                .onSuccess { version ->
                    _uiState.value = _uiState.value.copy(versionInfo = version)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(versionInfo = null)
                }
        }
    }

    fun testConnection(baseUrl: String, secret: String) {
        if (baseUrl.isBlank()) {
            _uiState.value = _uiState.value.copy(message = "Please enter API URL")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val config = ApiConfig(
                baseUrl = baseUrl.trimEnd('/'),
                secret = secret
            )

            repository.testConnection(config)
                .onSuccess { version ->
                    val versionText = if (version.meta) {
                        "Clash Meta ${version.version}"
                    } else {
                        "Clash ${version.version}"
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Connected: $versionText"
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Connection failed: ${e.message}"
                    )
                }
        }
    }

    fun connect(baseUrl: String, secret: String) {
        if (baseUrl.isBlank()) {
            _uiState.value = _uiState.value.copy(message = "Please enter API URL")
            return
        }

        viewModelScope.launch {
            val config = ApiConfig(
                baseUrl = baseUrl.trimEnd('/'),
                secret = secret
            )

            // Test connection first
            repository.testConnection(config)
                .onSuccess { version ->
                    // Save and set as current config
                    apiConfigDataStore.setCurrentConfig(config)
                    apiConfigDataStore.saveConfig(config)

                    _uiState.value = _uiState.value.copy(
                        currentConfig = config,
                        versionInfo = version,
                        message = "Connected successfully"
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        message = "Connection failed: ${e.message}"
                    )
                }
        }
    }

    fun removeConfig(config: ApiConfig) {
        viewModelScope.launch {
            apiConfigDataStore.removeConfig(config)
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
