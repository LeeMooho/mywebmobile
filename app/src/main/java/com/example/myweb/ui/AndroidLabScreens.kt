package com.example.myweb.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.content.ContextCompat
import com.example.myweb.R
import com.example.myweb.platform.AndroidPortfolioActions
import com.example.myweb.platform.DeviceSnapshot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidLabScreen(
    onBack: () -> Unit,
    onOpenDeviceInfo: () -> Unit,
    onOpenSystemTools: () -> Unit
) {
    Scaffold(topBar = { LabTopBar(R.string.android_lab, onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                stringResource(R.string.android_lab_intro),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.android_lab_privacy_note),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LabNavigationCard(
                title = stringResource(R.string.device_information),
                body = stringResource(R.string.device_information_description),
                icon = { Icon(Icons.Default.Phone, contentDescription = null) },
                testTag = "open_device_info",
                onClick = onOpenDeviceInfo
            )
            LabNavigationCard(
                title = stringResource(R.string.system_tools),
                body = stringResource(R.string.system_tools_description),
                icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                testTag = "open_system_tools",
                onClick = onOpenSystemTools
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceInfoScreen(snapshot: DeviceSnapshot, onBack: () -> Unit) {
    Scaffold(topBar = { LabTopBar(R.string.device_information, onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.device_info_safe_note),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            DeviceInfoRow(stringResource(R.string.device_manufacturer), snapshot.manufacturer)
            DeviceInfoRow(stringResource(R.string.device_model), snapshot.model)
            DeviceInfoRow(stringResource(R.string.android_version), snapshot.androidVersion)
            DeviceInfoRow(stringResource(R.string.sdk_level), snapshot.sdkLevel.toString())
            DeviceInfoRow(
                stringResource(R.string.screen_size),
                stringResource(R.string.screen_size_value, snapshot.screenWidthDp, snapshot.screenHeightDp)
            )
            DeviceInfoRow(stringResource(R.string.screen_density), snapshot.densityDpi.toString())
            DeviceInfoRow(stringResource(R.string.device_locale), snapshot.localeTag)
        }
    }
}

@Composable
fun SystemToolsScreen(actions: AndroidPortfolioActions, onBack: () -> Unit) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var feedbackResource by rememberSaveable { mutableStateOf<Int?>(null) }
    var notificationPermissionGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    fun postNotification() {
        feedbackResource = if (actions.showDemoNotification()) {
            R.string.notification_sent
        } else {
            R.string.notification_unavailable
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationPermissionGranted = granted
        if (granted) postNotification() else feedbackResource = R.string.notification_denied
    }

    SystemToolsContent(
        onBack = onBack,
        onCopy = {
            actions.copyPortfolioLink()
            feedbackResource = R.string.portfolio_link_copied
        },
        onShare = {
            actions.sharePortfolio()
            feedbackResource = R.string.share_sheet_opened
        },
        onHaptic = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            feedbackResource = R.string.haptic_played
        },
        onNotification = {
            if (notificationPermissionGranted) {
                postNotification()
            } else {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
        onOpenGitHub = actions::openGitHubProfile,
        feedbackResource = feedbackResource
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemToolsContent(
    onBack: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onHaptic: () -> Unit,
    onNotification: () -> Unit,
    onOpenGitHub: () -> Unit,
    feedbackResource: Int? = null
) {
    Scaffold(topBar = { LabTopBar(R.string.system_tools, onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.system_tools_intro),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LabActionCard(
                R.string.copy_portfolio_link,
                R.string.copy_portfolio_link_description,
                Icons.Default.List,
                "copy_portfolio_link",
                onCopy
            )
            LabActionCard(
                R.string.share_portfolio,
                R.string.share_portfolio_description,
                Icons.Default.Share,
                "share_portfolio",
                onShare
            )
            LabActionCard(
                R.string.haptic_feedback,
                R.string.haptic_feedback_description,
                Icons.Default.Phone,
                "play_haptic",
                onHaptic
            )
            LabActionCard(
                R.string.demo_notification,
                R.string.demo_notification_description,
                Icons.Default.Notifications,
                "show_notification",
                onNotification
            )
            LabActionCard(
                R.string.view_github,
                R.string.open_github_description,
                Icons.Default.Info,
                "open_github_from_lab",
                onOpenGitHub
            )
            feedbackResource?.let {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(it), modifier = Modifier.testTag("lab_feedback"))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabTopBar(title: Int, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(title)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
            }
        }
    )
}

@Composable
private fun LabNavigationCard(
    title: String,
    body: String,
    icon: @Composable () -> Unit,
    testTag: String,
    onClick: () -> Unit
) {
    OutlinedCard(Modifier.fillMaxWidth().testTag(testTag).clickable(onClick = onClick)) {
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
private fun LabActionCard(
    title: Int,
    body: Int,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(stringResource(body), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onClick, modifier = Modifier.testTag(testTag)) {
                Icon(icon, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.try_feature))
            }
        }
    }
}

@Composable
private fun DeviceInfoRow(label: String, value: String) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(16.dp))
            Text(value, fontWeight = FontWeight.Medium)
        }
    }
}
