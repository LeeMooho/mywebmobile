package com.example.myweb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myweb.api.ApiError
import com.example.myweb.api.ApiResult
import com.example.myweb.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val loading: Boolean = false,
    val invalidInput: Boolean = false,
    val error: ApiError? = null,
    val signedIn: Boolean = false
)

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(LoginUiState())
    val state = mutableState.asStateFlow()

    fun login(userId: String, password: String) {
        if (mutableState.value.loading || mutableState.value.signedIn) return
        if (userId.isBlank() || password.isBlank()) {
            mutableState.value = LoginUiState(invalidInput = true)
            return
        }
        mutableState.value = LoginUiState(loading = true)
        viewModelScope.launch {
            mutableState.value = when (val result = repository.login(userId.trim(), password)) {
                is ApiResult.Success -> LoginUiState(signedIn = true)
                is ApiResult.Failure -> LoginUiState(error = result.error)
            }
        }
    }
}
