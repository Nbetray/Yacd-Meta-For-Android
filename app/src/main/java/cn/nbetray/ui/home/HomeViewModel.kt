package cn.nbetray.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.nbetray.data.model.MemoryData
import cn.nbetray.data.model.TrafficData
import cn.nbetray.data.model.VersionInfo
import cn.nbetray.data.repository.ClashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val version: VersionInfo? = null,
    val traffic: TrafficData = TrafficData(0, 0),
    val memory: MemoryData = MemoryData(0),
    val totalUpload: Long = 0,
    val totalDownload: Long = 0,
    val activeConnections: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ClashRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _trafficHistory = MutableStateFlow<List<TrafficData>>(emptyList())
    val trafficHistory: StateFlow<List<TrafficData>> = _trafficHistory

    private val _memoryHistory = MutableStateFlow<List<MemoryData>>(emptyList())
    val memoryHistory: StateFlow<List<MemoryData>> = _memoryHistory

    private var trafficJob: Job? = null
    private var memoryJob: Job? = null
    private var connectionsJob: Job? = null

    init {
        loadVersion()
        startTrafficStream()
        startMemoryStream()
        startConnectionsStream()
    }

    fun loadVersion() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getVersion()
                .onSuccess { version ->
                    _uiState.value = _uiState.value.copy(
                        version = version,
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

    fun startTrafficStream() {
        trafficJob?.cancel()
        trafficJob = viewModelScope.launch {
            try {
                repository.trafficFlow()
                    .catch { e ->
                        _uiState.value = _uiState.value.copy(error = e.message)
                    }
                    .collect { traffic ->
                        _uiState.value = _uiState.value.copy(traffic = traffic)

                        // Keep last 1 minute (60 data points) for chart
                        val history = _trafficHistory.value.toMutableList()
                        history.add(traffic)
                        if (history.size > 60) {
                            history.removeAt(0)
                        }
                        _trafficHistory.value = history
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun startMemoryStream() {
        memoryJob?.cancel()
        memoryJob = viewModelScope.launch {
            try {
                repository.memoryFlow()
                    .catch { e ->
                        // Ignore memory errors
                    }
                    .collect { memory ->
                        _uiState.value = _uiState.value.copy(memory = memory)

                        // Keep last 1 minute (60 data points) for chart
                        val history = _memoryHistory.value.toMutableList()
                        history.add(memory)
                        if (history.size > 60) {
                            history.removeAt(0)
                        }
                        _memoryHistory.value = history
                    }
            } catch (e: Exception) {
                // Ignore memory errors
            }
        }
    }

    fun startConnectionsStream() {
        connectionsJob?.cancel()
        connectionsJob = viewModelScope.launch {
            try {
                repository.connectionsFlow()
                    .catch { e ->
                        // Ignore connections errors
                    }
                    .collect { response ->
                        _uiState.value = _uiState.value.copy(
                            totalUpload = response.uploadTotal,
                            totalDownload = response.downloadTotal,
                            activeConnections = response.connections?.size ?: 0
                        )
                    }
            } catch (e: Exception) {
                // Ignore connections errors
            }
        }
    }

    fun refresh() {
        loadVersion()
        startTrafficStream()
        startMemoryStream()
        startConnectionsStream()
    }

    override fun onCleared() {
        super.onCleared()
        trafficJob?.cancel()
        memoryJob?.cancel()
        connectionsJob?.cancel()
    }
}
