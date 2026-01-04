package cn.nbetray.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.settingsDataStore

    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val LATENCY_TEST_URL = stringPreferencesKey("latency_test_url")
        private val AUTO_CLOSE_OLD_CONNS = booleanPreferencesKey("auto_close_old_conns")
        private val HIDE_UNAVAILABLE_PROXIES = booleanPreferencesKey("hide_unavailable_proxies")
        private val LANGUAGE = stringPreferencesKey("language")

        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"

        const val LANG_SYSTEM = "system"
        const val LANG_ZH = "zh"
        const val LANG_EN = "en"

        const val DEFAULT_LATENCY_URL = "https://www.gstatic.com/generate_204"
    }

    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: THEME_SYSTEM
    }

    val language: Flow<String> = dataStore.data.map { preferences ->
        preferences[LANGUAGE] ?: LANG_SYSTEM
    }

    val latencyTestUrl: Flow<String> = dataStore.data.map { preferences ->
        preferences[LATENCY_TEST_URL] ?: DEFAULT_LATENCY_URL
    }

    val autoCloseOldConns: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUTO_CLOSE_OLD_CONNS] ?: false
    }

    val hideUnavailableProxies: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[HIDE_UNAVAILABLE_PROXIES] ?: false
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setLatencyTestUrl(url: String) {
        dataStore.edit { preferences ->
            preferences[LATENCY_TEST_URL] = url
        }
    }

    suspend fun setAutoCloseOldConns(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_CLOSE_OLD_CONNS] = enabled
        }
    }

    suspend fun setHideUnavailableProxies(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[HIDE_UNAVAILABLE_PROXIES] = enabled
        }
    }

    suspend fun setLanguage(lang: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE] = lang
        }
    }
}
