package cn.nbetray

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import cn.nbetray.data.local.SettingsDataStore
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class YacdApplication : Application() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate() {
        super.onCreate()

        // Apply saved language on app start
        runBlocking {
            val lang = settingsDataStore.language.first()
            applyLanguage(lang)
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
}
