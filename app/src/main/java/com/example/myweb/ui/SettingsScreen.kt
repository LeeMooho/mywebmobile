package com.example.myweb.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myweb.utils.LanguagePreference
import com.example.myweb.utils.LocaleHelper

@Composable
fun SettingsScreen(activity: Activity? = null,
                   onLanguageChanged: () -> Unit = {}) {
    val context = LocalContext.current
    val languages = listOf("en" to "English", "ko" to "한국어", "ja" to "日本語")

    var selectedLang by remember {
        mutableStateOf(LanguagePreference.getSavedLanguageFromPreferences(context) ?: "en")
    }

    fun changeLanguage(code: String) {
        if (selectedLang != code) {
            selectedLang = code
            LanguagePreference.saveLanguageToPreferences(context, code)
            LocaleHelper.setLocale(context, code)

            Toast.makeText(
                context,
                "Please, restart Application",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Text("언어 설정", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        languages.forEach { (code, label) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { changeLanguage(code) }
            ) {
                RadioButton(
                    selected = selectedLang == code,
                    onClick = { changeLanguage(code) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label)
            }
        }
    }
}
