package com.example.myweb.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myweb.R

@Composable
fun HomeScreen(onBoardClick: () -> Unit = {}, onSettingsClick: () -> Unit = {}) {
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("MYWEB", color = colors.primary, style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.home_title), style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.home_subtitle), color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge)
        }
        Card(shape = RoundedCornerShape(28.dp)) {
            Column(
                Modifier.fillMaxWidth().background(Brush.linearGradient(
                    listOf(colors.primaryContainer, colors.tertiaryContainer)))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(Icons.Default.List, contentDescription = null,
                    tint = colors.onPrimaryContainer, modifier = Modifier.size(40.dp))
                Text(stringResource(R.string.home_community), color = colors.onPrimaryContainer,
                    style = MaterialTheme.typography.labelLarge)
                Text(stringResource(R.string.home_feature_title), color = colors.onPrimaryContainer,
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.home_feature_body), color = colors.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyLarge)
                Button(onClick = onBoardClick, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.home_explore))
                }
            }
        }
        OutlinedCard(shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = colors.primary)
                Text(stringResource(R.string.home_settings_title),
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.home_settings_body), color = colors.onSurfaceVariant)
                TextButton(onClick = onSettingsClick) {
                    Text(stringResource(R.string.home_personalize))
                }
            }
        }
    }
}
