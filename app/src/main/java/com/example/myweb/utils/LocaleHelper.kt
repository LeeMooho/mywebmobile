package com.example.myweb.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    fun setLocale(context: Context, language: String): Context {
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale(language))
        return context.createConfigurationContext(config)
    }
}
