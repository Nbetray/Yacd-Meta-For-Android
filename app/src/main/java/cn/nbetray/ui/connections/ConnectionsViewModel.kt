package cn.nbetray.ui.connections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.nbetray.data.model.Connection
import cn.nbetray.data.repository.ClashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConnectionsUiState(
    val connections: List<Connection> = emptyList(),
    val uploadTotal: Long = 0,
    val downloadTotal: Long = 0,
    val isLoading: Boolean = false,
    val isPaused: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ConnectionsViewModel @Inject constructor(
    private val repository: ClashRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectionsUiState())
    val uiState: StateFlow<ConnectionsUiState> = _uiState

    private var connectionsJob: Job? = null

    init {
        startConnectionsStream()
    }

    fun startConnectionsStream() {
        if (_uiState.value.isPaused) return

        connectionsJob?.cancel()
        connectionsJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.connectionsFlow()
                    .catch { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
                    .collect { response ->
                        _uiState.value = _uiState.value.copy(
                            connections = response.connections ?: emptyList(),
                            uploadTotal = response.uploadTotal,
                            downloadTotal = response.downloadTotal,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun togglePause() {
        val isPaused = !_uiState.value.isPaused
        _uiState.value = _uiState.value.copy(isPaused = isPaused)

        if (isPaused) {
            connectionsJob?.cancel()
        } else {
            startConnectionsStream()
        }
    }

    fun closeConnection(connectionId: String) {
        viewModelScope.launch {
            repository.closeConnection(connectionId)
        }
    }

    fun closeAllConnections() {
        viewModelScope.launch {
            repository.closeAllConnections()
        }
    }

    override fun onCleared() {
        super.onCleared()
        connectionsJob?.cancel()
    }
}
