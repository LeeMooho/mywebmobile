package com.example.myweb.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SettingsRepositoryTest {
    private val preferences get() = ApplicationProvider.getApplicationContext<Context>()
        .getSharedPreferences("settings-test", Context.MODE_PRIVATE)
    @Before fun clear() { preferences.edit().clear().commit() }

    @Test fun defaultsAreEnglishAndSystemTheme() {
        assertEquals(AppSettings(), SettingsRepository(preferences).settings.value)
    }

    @Test fun languageAndThemeSurviveRepositoryRecreation() {
        val repository = SettingsRepository(preferences)
        repository.setLanguage("ko")
        repository.setTheme(ThemeMode.DARK)
        assertEquals(AppSettings("ko", ThemeMode.DARK), repository.settings.value)
        assertEquals(repository.settings.value, SettingsRepository(preferences).settings.value)
    }

    @Test fun invalidStoredValuesFallBackToSupportedDefaults() {
        preferences.edit().putString("language", "invalid").putString("theme", "invalid").commit()
        assertEquals(AppSettings(), SettingsRepository(preferences).settings.value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun unsupportedLanguageIsRejected() { SettingsRepository(preferences).setLanguage("invalid") }
}
