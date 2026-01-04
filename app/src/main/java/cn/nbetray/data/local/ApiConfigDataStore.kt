package cn.nbetray.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "api_config")

data class ApiConfig(
    val baseUrl: String = "http://127.0.0.1:9090",
    val secret: String = ""
) {
    val displayName: String
        get() = baseUrl.removePrefix("http://").removePrefix("https://")
}

@Singleton
class ApiConfigDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val dataStore = context.dataStore

    companion object {
        private val CURRENT_API_CONFIG = stringPreferencesKey("current_api_config")
        private val SAVED_API_CONFIGS = stringPreferencesKey("saved_api_configs")
    }

    val currentConfig: Flow<ApiConfig> = dataStore.data.map { preferences ->
        val json = preferences[CURRENT_API_CONFIG]
        if (json != null) {
            try {
                gson.fromJson(json, ApiConfig::class.java)
            } catch (e: Exception) {
                ApiConfig()
            }
        } else {
            ApiConfig()
        }
    }

    val savedConfigs: Flow<List<ApiConfig>> = dataStore.data.map { preferences ->
        val json = preferences[SAVED_API_CONFIGS]
        if (json != null) {
            try {
                val type = object : TypeToken<List<ApiConfig>>() {}.type
                gson.fromJson(json, type)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    suspend fun setCurrentConfig(config: ApiConfig) {
        dataStore.edit { preferences ->
            preferences[CURRENT_API_CONFIG] = gson.toJson(config)
        }
    }

    suspend fun saveConfig(config: ApiConfig) {
        dataStore.edit { preferences ->
            val currentList = preferences[SAVED_API_CONFIGS]?.let {
                try {
                    val type = object : TypeToken<List<ApiConfig>>() {}.type
                    gson.fromJson<List<ApiConfig>>(it, type).toMutableList()
                } catch (e: Exception) {
                    mutableListOf()
                }
            } ?: mutableListOf()

            // Remove existing config with same baseUrl
            currentList.removeAll { it.baseUrl == config.baseUrl }
            currentList.add(0, config)

            // Keep only last 10 configs
            val trimmedList = currentList.take(10)
            preferences[SAVED_API_CONFIGS] = gson.toJson(trimmedList)
        }
    }

    suspend fun removeConfig(config: ApiConfig) {
        dataStore.edit { preferences ->
            val currentList = preferences[SAVED_API_CONFIGS]?.let {
                try {
                    val type = object : TypeToken<List<ApiConfig>>() {}.type
                    gson.fromJson<List<ApiConfig>>(it, type).toMutableList()
                } catch (e: Exception) {
                    mutableListOf()
                }
            } ?: mutableListOf()

            currentList.removeAll { it.baseUrl == config.baseUrl }
            preferences[SAVED_API_CONFIGS] = gson.toJson(currentList)
        }
    }
}
