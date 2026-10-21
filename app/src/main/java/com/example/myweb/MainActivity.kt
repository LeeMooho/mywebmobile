package com.example.myweb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.*
import com.example.myweb.data.AppContainer
import com.example.myweb.data.ThemeMode
import com.example.myweb.data.viewModelFactory
import com.example.myweb.platform.AndroidPortfolioActions
import com.example.myweb.ui.*
import com.example.myweb.ui.theme.MywebTheme
import com.example.myweb.utils.LocaleHelper
import com.example.myweb.viewmodel.BoardViewModel
import com.example.myweb.viewmodel.BoardDetailViewModel
import com.example.myweb.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppRoot() }
    }
}

@Composable
fun AppRoot(container: AppContainer = (LocalContext.current.applicationContext as MyApplication).container) {
    val settings by container.settings.settings.collectAsState()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val localizedContext = remember(context, configuration, settings.language) {
        LocaleHelper.setLocale(context, settings.language)
    }
    val darkTheme = when (settings.theme) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedContext.resources.configuration
    ) {
        MywebTheme(darkTheme = darkTheme) { MainScreen(container) }
    }
}

@Composable
fun MainScreen(container: AppContainer = (LocalContext.current.applicationContext as MyApplication).container) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val settings by container.settings.settings.collectAsState()
    val session by container.sessions.session.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val portfolioActions = remember(context) { AndroidPortfolioActions(context) }
    var signingOut by remember { mutableStateOf(false) }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(bottomBar = {
        if (currentRoute in setOf("home", "board", "my-page")) {
            NavigationBar {
                listOf(
                    Triple("home", R.string.home, Icons.Default.Home),
                    Triple("board", R.string.board, Icons.Default.List),
                    Triple("my-page", R.string.my_page, Icons.Default.Person)
                ).forEach { (route, label, icon) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = null) },
                        label = { Text(stringResource(label)) },
                        selected = currentRoute == route,
                        onClick = { navigateTo(route) }
                    )
                }
            }
        }
    }) { padding ->
        NavHost(navController, startDestination = "home", modifier = Modifier.padding(padding)) {
            composable("home") {
                HomeScreen(
                    onBoardClick = { navigateTo("board") },
                    onSettingsClick = { navController.navigate("settings") { launchSingleTop = true } }
                )
            }
            composable("board") {
                val vm: BoardViewModel = viewModel(factory = viewModelFactory { BoardViewModel(container.boards) })
                BoardApp(vm, onBoardClick = { boardNo ->
                    navController.navigate("board/$boardNo") { launchSingleTop = true }
                })
            }
            composable(
                route = "board/{boardNo}",
                arguments = listOf(navArgument("boardNo") { type = NavType.LongType })
            ) { entry ->
                val boardNo = entry.arguments?.getLong("boardNo") ?: return@composable
                val vm: BoardDetailViewModel = viewModel(
                    key = "board-detail-$boardNo",
                    factory = viewModelFactory { BoardDetailViewModel(boardNo, container.boards) }
                )
                BoardDetailApp(vm, onBack = { navController.popBackStack() })
            }
            composable("my-page") {
                MyPageScreen(
                    session = session,
                    signingOut = signingOut,
                    onLogin = { navController.navigate("login") { launchSingleTop = true } },
                    onLogout = {
                        if (!signingOut) scope.launch {
                            signingOut = true
                            try { container.auth.logout() } finally { signingOut = false }
                        }
                    },
                    onOpenSettings = { navController.navigate("settings") { launchSingleTop = true } },
                    onOpenAndroidLab = { navController.navigate("android-lab") { launchSingleTop = true } },
                    onOpenGitHub = portfolioActions::openGitHubProfile
                )
            }
            composable("settings") {
                SettingsScreen(
                    settings = settings,
                    onLanguageChanged = container.settings::setLanguage,
                    onThemeChanged = container.settings::setTheme,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("android-lab") {
                AndroidLabScreen(
                    onBack = { navController.popBackStack() },
                    onOpenDeviceInfo = {
                        navController.navigate("android-lab/device") { launchSingleTop = true }
                    },
                    onOpenSystemTools = {
                        navController.navigate("android-lab/system-tools") { launchSingleTop = true }
                    }
                )
            }
            composable("android-lab/device") {
                DeviceInfoScreen(
                    snapshot = remember(portfolioActions) { portfolioActions.deviceSnapshot() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("android-lab/system-tools") {
                SystemToolsScreen(portfolioActions, onBack = { navController.popBackStack() })
            }
            composable("login") {
                val vm: LoginViewModel = viewModel(factory = viewModelFactory { LoginViewModel(container.auth) })
                val state by vm.state.collectAsState()
                LoginScreen(state, vm::login, onBack = { navController.popBackStack() })
                LaunchedEffect(state.signedIn) {
                    if (state.signedIn) navController.popBackStack()
                }
            }
        }
    }
}
