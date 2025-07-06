package com.example.myweb.utils

import android.content.Context
import android.content.SharedPreferences

object LanguagePreference {
    private const val PREF_NAME = "app_preferences"
    private const val KEY_LANGUAGE = "language"

    fun saveLanguageToPreferences(context: Context, languageCode: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    fun getSavedLanguageFromPreferences(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, null)
    }
}
