package com.example.myweb.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserSession(val userId: String, val displayName: String, val token: String) {
    override fun toString() = "UserSession(<redacted>)"
}

// Session tokens stay in memory and are never stored in preferences or backups.
class SessionStore {
    private val mutableSession = MutableStateFlow<UserSession?>(null)
    val session = mutableSession.asStateFlow()

    fun signIn(session: UserSession) { mutableSession.value = session }
    fun clear() { mutableSession.value = null }
    fun clearIfTokenMatches(token: String) {
        val current = mutableSession.value
        if (current?.token == token) mutableSession.compareAndSet(current, null)
    }
}
