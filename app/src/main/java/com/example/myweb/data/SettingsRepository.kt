package com.example.myweb.data

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(val language: String = "en", val theme: ThemeMode = ThemeMode.SYSTEM)

class SettingsRepository(private val preferences: SharedPreferences) {
    companion object { val languages = listOf("en", "ko", "ja") }
    private val mutableSettings = MutableStateFlow(AppSettings(
        language = preferences.getString("language", null)?.takeIf { it in languages } ?: "en",
        theme = ThemeMode.values().find { it.name == preferences.getString("theme", null) } ?: ThemeMode.SYSTEM
    ))
    val settings = mutableSettings.asStateFlow()

    fun setLanguage(language: String) {
        require(language in languages)
        preferences.edit().putString("language", language).apply()
        mutableSettings.value = mutableSettings.value.copy(language = language)
    }

    fun setTheme(theme: ThemeMode) {
        preferences.edit().putString("theme", theme.name).apply()
        mutableSettings.value = mutableSettings.value.copy(theme = theme)
    }
}
