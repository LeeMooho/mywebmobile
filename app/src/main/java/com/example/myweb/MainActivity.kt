package com.example.myweb

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.compose.ui.res.stringResource
import com.example.myweb.utils.LanguagePreference
import com.example.myweb.utils.LocaleHelper

import com.example.myweb.ui.HomeScreen
import com.example.myweb.ui.BoardApp
import com.example.myweb.ui.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val lang = LanguagePreference.getSavedLanguageFromPreferences(this) ?: "en"
        val localizedContext = LocaleHelper.setLocale(this, lang)

        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(
                LocalContext provides localizedContext
            ) {
                MainScreen()
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        val lang = LanguagePreference.getSavedLanguageFromPreferences(base) ?: "en"
        val localized = LocaleHelper.setLocale(base, lang)
        super.attachBaseContext(localized)
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.home)) },
                    selected = navController.currentDestination?.route == "home",
                    onClick = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text(stringResource(R.string.board)) },
                    selected = navController.currentDestination?.route == "board",
                    onClick = {
                        navController.navigate("board") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.settings)) },
                    selected = navController.currentDestination?.route == "settings",
                    onClick = {
                        navController.navigate("settings") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen() }
            composable("board") { BoardApp() }
            composable("settings") { SettingsScreen() }
          }

    }
}