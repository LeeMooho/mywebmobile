package com.example.myweb.data

import com.example.myweb.api.ApiError
import com.example.myweb.api.ApiResult
import com.example.myweb.api.AuthApi
import com.example.myweb.api.apiCall
import com.example.myweb.model.LoginRequest

interface AuthRepository {
    suspend fun login(userId: String, password: String): ApiResult<UserSession>
    suspend fun logout(): ApiResult<Unit>
}

class RemoteAuthRepository(
    private val api: AuthApi,
    private val sessions: SessionStore,
    private val secureTransport: Boolean
) : AuthRepository {
    override suspend fun login(userId: String, password: String): ApiResult<UserSession> {
        if (!secureTransport) return ApiResult.Failure(ApiError.SECURE_CONNECTION_REQUIRED)
        return when (val result = apiCall { api.login(LoginRequest(userId, password)) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> {
                val user = result.value.item
                val id = user?.userId
                val token = user?.token
                if (id.isNullOrBlank() || token.isNullOrBlank()) {
                    ApiResult.Failure(ApiError.INVALID_RESPONSE)
                } else {
                    val session = UserSession(id, user.userName?.takeIf { it.isNotBlank() } ?: id, token)
                    sessions.signIn(session)
                    ApiResult.Success(session)
                }
            }
        }
    }

    override suspend fun logout(): ApiResult<Unit> {
        try {
            if (!secureTransport) return ApiResult.Failure(ApiError.SECURE_CONNECTION_REQUIRED)
            return when (val result = apiCall { api.logout() }) {
                is ApiResult.Failure -> result
                is ApiResult.Success -> ApiResult.Success(Unit)
            }
        } finally {
            // Always discard the local session, even when the server cannot be reached.
            sessions.clear()
        }
    }
}
