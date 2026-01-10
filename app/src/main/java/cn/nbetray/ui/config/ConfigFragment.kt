package cn.nbetray.ui.config

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cn.nbetray.R
import cn.nbetray.data.local.SettingsDataStore
import cn.nbetray.databinding.FragmentConfigBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConfigFragment : Fragment() {

    private var _binding: FragmentConfigBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConfigViewModel by viewModels()

    // Flags to prevent listener triggering during programmatic updates
    private var isUpdatingLanguageChip = false
    private var isUpdatingModeChip = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLanguageChips()
        setupLogLevelSpinner()
        setupModeChips()
        setupActionButtons()
        observeState()
    }

    private fun setupLanguageChips() {
        binding.languageChips.setOnCheckedStateChangeListener { _, checkedIds ->
            if (isUpdatingLanguageChip) return@setOnCheckedStateChangeListener

            val lang = when (checkedIds.firstOrNull()) {
                R.id.chip_lang_system -> SettingsDataStore.LANG_SYSTEM
                R.id.chip_lang_zh -> SettingsDataStore.LANG_ZH
                R.id.chip_lang_en -> SettingsDataStore.LANG_EN
                else -> return@setOnCheckedStateChangeListener
            }

            // Check if it's actually a change
            if (lang != viewModel.uiState.value.language) {
                viewModel.setLanguage(lang)
                applyLanguage(lang)
            }
        }
    }

    private fun applyLanguage(lang: String) {
        val localeList = when (lang) {
            SettingsDataStore.LANG_ZH -> LocaleListCompat.forLanguageTags("zh")
            SettingsDataStore.LANG_EN -> LocaleListCompat.forLanguageTags("en")
            else -> LocaleListCompat.getEmptyLocaleList() // Follow system
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    private fun setupLogLevelSpinner() {
        val levels = listOf("debug", "info", "warning", "error", "silent")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, levels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.logLevelSpinner.adapter = adapter
    }

    private fun setupModeChips() {
        binding.modeChips.setOnCheckedStateChangeListener { _, checkedIds ->
            if (isUpdatingModeChip) return@setOnCheckedStateChangeListener

            val mode = when (checkedIds.firstOrNull()) {
                R.id.chip_rule -> "rule"
                R.id.chip_global -> "global"
                R.id.chip_direct -> "direct"
                else -> return@setOnCheckedStateChangeListener
            }

            // Check if it's actually a change
            if (mode != viewModel.uiState.value.config?.mode?.lowercase()) {
                viewModel.updateMode(mode)
            }
        }
    }

    private fun setupActionButtons() {
        binding.allowLan.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateAllowLan(isChecked)
        }

        binding.btnReloadConfig.setOnClickListener {
            viewModel.reloadConfig()
        }

        binding.btnRestart.setOnClickListener {
            viewModel.restart()
        }

        binding.btnUpgradeGeo.setOnClickListener {
            viewModel.upgradeGeo()
        }

        binding.btnFlushFakeip.setOnClickListener {
            viewModel.flushFakeIp()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Set language chip with flag to prevent listener loop
                    isUpdatingLanguageChip = true
                    when (state.language) {
                        SettingsDataStore.LANG_ZH -> binding.chipLangZh.isChecked = true
                        SettingsDataStore.LANG_EN -> binding.chipLangEn.isChecked = true
                        else -> binding.chipLangSystem.isChecked = true
                    }
                    isUpdatingLanguageChip = false

                    state.config?.let { config ->
                        binding.httpPort.setText(config.port.toString())
                        binding.socksPort.setText(config.socksPort.toString())
                        binding.mixedPort.setText(config.mixedPort.toString())
                        binding.allowLan.isChecked = config.allowLan

                        // Set mode chip with flag to prevent listener loop
                        isUpdatingModeChip = true
                        when (config.mode.lowercase()) {
                            "rule" -> binding.chipRule.isChecked = true
                            "global" -> binding.chipGlobal.isChecked = true
                            "direct" -> binding.chipDirect.isChecked = true
                        }
                        isUpdatingModeChip = false

                        // Set log level
                        val levels = listOf("debug", "info", "warning", "error", "silent")
                        val levelIndex = levels.indexOf(config.logLevel.lowercase())
                        if (levelIndex >= 0) {
                            binding.logLevelSpinner.setSelection(levelIndex)
                        }
                    }

                    state.message?.let { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        viewModel.clearMessage()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
