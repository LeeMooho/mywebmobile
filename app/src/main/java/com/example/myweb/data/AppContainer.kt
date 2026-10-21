package com.example.myweb.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myweb.api.ApiClient

class AppContainer(context: Context) {
    val settings = SettingsRepository(context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE))
    val sessions = SessionStore()
    private val client by lazy { ApiClient(sessions) }
    val boards: BoardRepository by lazy { RemoteBoardRepository(client.boards) }
    val auth: AuthRepository by lazy { RemoteAuthRepository(client.auth, sessions, client.url.isHttps) }
}

fun <T : ViewModel> viewModelFactory(create: () -> T): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = create() as VM
    }
