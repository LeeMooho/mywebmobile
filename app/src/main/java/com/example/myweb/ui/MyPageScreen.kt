package com.example.myweb.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myweb.R
import com.example.myweb.data.UserSession

@Composable
fun MyPageScreen(
    session: UserSession?,
    signingOut: Boolean,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAndroidLab: () -> Unit,
    onOpenGitHub: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            stringResource(R.string.my_page),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        ProfileHero(onOpenGitHub)
        PortfolioProjectCard()
        Text(stringResource(R.string.portfolio_explore), style = MaterialTheme.typography.titleLarge)
        PortfolioNavigationCard(
            title = stringResource(R.string.android_lab),
            body = stringResource(R.string.android_lab_description),
            icon = { Icon(Icons.Default.Star, contentDescription = null) },
            testTag = "open_android_lab",
            onClick = onOpenAndroidLab
        )
        PortfolioNavigationCard(
            title = stringResource(R.string.app_settings),
            body = stringResource(R.string.app_settings_description),
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            testTag = "open_app_settings",
            onClick = onOpenSettings
        )
        AccountCard(session, signingOut, onLogin, onLogout)
    }
}

@Composable
private fun ProfileHero(onOpenGitHub: () -> Unit) {
    Card(shape = RoundedCornerShape(28.dp)) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier.size(84.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("LM", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Text(stringResource(R.string.portfolio_name), style = MaterialTheme.typography.headlineSmall)
            Text(
                stringResource(R.string.portfolio_role),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                stringResource(R.string.portfolio_intro),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(onClick = onOpenGitHub, modifier = Modifier.testTag("open_github")) {
                Text(stringResource(R.string.view_github))
            }
        }
    }
}

@Composable
private fun PortfolioProjectCard() {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.featured_project), style = MaterialTheme.typography.labelLarge)
            }
            Text("MyWeb Mobile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.myweb_project_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Kotlin  •  Jetpack Compose  •  REST API  •  Robolectric",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PortfolioNavigationCard(
    title: String,
    body: String,
    icon: @Composable () -> Unit,
    testTag: String,
    onClick: () -> Unit
) {
    OutlinedCard(
        Modifier.fillMaxWidth().testTag(testTag).clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            icon()
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AccountCard(
    session: UserSession?,
    signingOut: Boolean,
    onLogin: () -> Unit,
    onLogout: () -> Unit
) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.account), style = MaterialTheme.typography.titleLarge)
            }
            Text(
                session?.let { stringResource(R.string.signed_in_as, it.displayName) }
                    ?: stringResource(R.string.account_guest),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (session == null) {
                Button(onClick = onLogin) { Text(stringResource(R.string.login)) }
            } else {
                OutlinedButton(onClick = onLogout, enabled = !signingOut) {
                    Text(stringResource(if (signingOut) R.string.signing_out else R.string.logout))
                }
            }
        }
    }
}
