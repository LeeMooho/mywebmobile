package com.example.myweb

import android.app.Application
import android.content.Context
import com.example.myweb.utils.LanguagePreference
import com.example.myweb.utils.LocaleHelper

class MyApplication : Application() {
    override fun attachBaseContext(base: Context) {
        val lang = LanguagePreference.getSavedLanguageFromPreferences(base) ?: "en"
        val localizedContext = LocaleHelper.setLocale(base, lang)
        super.attachBaseContext(localizedContext)
    }

    override fun onCreate() {
        super.onCreate()
        val lang = LanguagePreference.getSavedLanguageFromPreferences(this) ?: "en"
        LocaleHelper.setLocale(this, lang)
    }
}
