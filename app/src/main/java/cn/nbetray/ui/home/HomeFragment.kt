package cn.nbetray.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cn.nbetray.databinding.FragmentHomeBinding
import cn.nbetray.util.FormatUtils
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChart()
        setupSwipeRefresh()
        observeState()
    }

    // Custom formatter for Y-axis (display as KB or MB)
    private val bytesAxisFormatter = object : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return FormatUtils.formatChartBytes(value)
        }
    }

    private fun setupChart() {
        binding.trafficChart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setTouchEnabled(false)
            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawLabels(false)
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
                valueFormatter = bytesAxisFormatter
            }

            axisRight.isEnabled = false
        }

        binding.memoryChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawLabels(false)
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }

            axisRight.isEnabled = false
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        binding.swipeRefresh.isRefreshing = state.isLoading

                        // Update traffic speeds
                        binding.uploadSpeed.text = FormatUtils.formatSpeed(state.traffic.up)
                        binding.downloadSpeed.text = FormatUtils.formatSpeed(state.traffic.down)

                        // Update total upload/download
                        binding.totalUpload.text = FormatUtils.formatBytes(state.totalUpload)
                        binding.totalDownload.text = FormatUtils.formatBytes(state.totalDownload)
                        binding.activeConnections.text = state.activeConnections.toString()

                        // Update memory
                        binding.memoryUsage.text = FormatUtils.formatBytes(state.memory.inuse)

                        // Update version
                        state.version?.let { version ->
                            val versionText = if (version.meta) {
                                "Clash Meta ${version.version}"
                            } else {
                                "Clash ${version.version}"
                            }
                            binding.versionInfo.text = versionText
                        }
                    }
                }

                launch {
                    viewModel.trafficHistory.collect { history ->
                        updateTrafficChart(history)
                    }
                }

                launch {
                    viewModel.memoryHistory.collect { history ->
                        updateMemoryChart(history)
                    }
                }
            }
        }
    }

    private fun updateTrafficChart(history: List<cn.nbetray.data.model.TrafficData>) {
        if (history.isEmpty()) return

        val uploadEntries = history.mapIndexed { index, data ->
            Entry(index.toFloat(), data.up.toFloat())
        }

        val downloadEntries = history.mapIndexed { index, data ->
            Entry(index.toFloat(), data.down.toFloat())
        }

        // Find max value in current data for dynamic Y-axis scaling
        val maxUp = history.maxOfOrNull { it.up } ?: 0L
        val maxDown = history.maxOfOrNull { it.down } ?: 0L
        val maxValue = maxOf(maxUp, maxDown)

        val uploadDataSet = LineDataSet(uploadEntries, "Upload").apply {
            color = Color.parseColor("#6366F1")
            setDrawCircles(false)
            setDrawValues(false)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val downloadDataSet = LineDataSet(downloadEntries, "Download").apply {
            color = Color.parseColor("#22C55E")
            setDrawCircles(false)
            setDrawValues(false)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        binding.trafficChart.apply {
            // Dynamically update Y-axis max based on current 1-minute data
            axisLeft.axisMaximum = if (maxValue > 0) maxValue * 1.1f else 1f
            data = LineData(uploadDataSet, downloadDataSet)
            invalidate()
        }
    }

    private fun updateMemoryChart(history: List<cn.nbetray.data.model.MemoryData>) {
        if (history.isEmpty()) return

        val memoryEntries = history.mapIndexed { index, data ->
            Entry(index.toFloat(), data.inuse.toFloat() / (1024 * 1024)) // Convert to MB
        }

        val memoryDataSet = LineDataSet(memoryEntries, "Memory").apply {
            color = Color.parseColor("#F59E0B")
            setDrawCircles(false)
            setDrawValues(false)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#F59E0B")
            fillAlpha = 50
        }

        binding.memoryChart.data = LineData(memoryDataSet)
        binding.memoryChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
