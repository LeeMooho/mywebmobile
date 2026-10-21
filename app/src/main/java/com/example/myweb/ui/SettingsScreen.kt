package com.example.myweb.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myweb.BuildConfig
import com.example.myweb.R
import com.example.myweb.data.AppSettings
import com.example.myweb.data.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onLanguageChanged: (String) -> Unit,
    onThemeChanged: (ThemeMode) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.settings)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            }
        )
    }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(stringResource(R.string.language), style = MaterialTheme.typography.titleLarge)
            Column(Modifier.selectableGroup()) {
                listOf("en" to "English", "ko" to "한국어", "ja" to "日本語").forEach { (code, label) ->
                    SettingsChoice(label, settings.language == code) { onLanguageChanged(code) }
                }
            }
            Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleLarge)
            Column(Modifier.selectableGroup()) {
                ThemeMode.values().forEach { mode ->
                    val label = when (mode) {
                        ThemeMode.SYSTEM -> R.string.theme_system
                        ThemeMode.LIGHT -> R.string.theme_light
                        ThemeMode.DARK -> R.string.theme_dark
                    }
                    SettingsChoice(stringResource(label), settings.theme == mode) { onThemeChanged(mode) }
                }
            }
            Text(
                stringResource(R.string.app_version, BuildConfig.VERSION_NAME),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SettingsChoice(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().selectable(selected, onClick = onClick, role = Role.RadioButton)
        .padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(12.dp))
        Text(label)
    }
}
