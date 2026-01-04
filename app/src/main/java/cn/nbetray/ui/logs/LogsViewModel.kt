package cn.nbetray.ui.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.nbetray.data.model.LogEntry
import cn.nbetray.data.repository.ClashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogsUiState(
    val logs: List<LogEntry> = emptyList(),
    val logLevel: String = "info",
    val isPaused: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val repository: ClashRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState

    private var logsJob: Job? = null
    private val maxLogs = 500

    init {
        startLogsStream()
    }

    fun startLogsStream() {
        if (_uiState.value.isPaused) return

        logsJob?.cancel()
        logsJob = viewModelScope.launch {
            try {
                repository.logsFlow(_uiState.value.logLevel)
                    .catch { e ->
                        _uiState.value = _uiState.value.copy(error = e.message)
                    }
                    .collect { log ->
                        val currentLogs = _uiState.value.logs.toMutableList()
                        currentLogs.add(log)

                        // Keep only last maxLogs entries
                        if (currentLogs.size > maxLogs) {
                            currentLogs.removeAt(0)
                        }

                        _uiState.value = _uiState.value.copy(logs = currentLogs)
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun setLogLevel(level: String) {
        _uiState.value = _uiState.value.copy(logLevel = level, logs = emptyList())
        startLogsStream()
    }

    fun togglePause() {
        val isPaused = !_uiState.value.isPaused
        _uiState.value = _uiState.value.copy(isPaused = isPaused)

        if (isPaused) {
            logsJob?.cancel()
        } else {
            startLogsStream()
        }
    }

    fun clearLogs() {
        _uiState.value = _uiState.value.copy(logs = emptyList())
    }

    override fun onCleared() {
        super.onCleared()
        logsJob?.cancel()
    }
}
