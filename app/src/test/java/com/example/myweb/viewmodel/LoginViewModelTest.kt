package com.example.myweb.viewmodel

import com.example.myweb.api.ApiError
import com.example.myweb.api.ApiResult
import com.example.myweb.data.AuthRepository
import com.example.myweb.data.UserSession
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private class Auth : AuthRepository {
        var calls = 0
        var credentials: Pair<String, String>? = null
        var result: ApiResult<UserSession> = ApiResult.Success(UserSession("test", "Test", "test-token"))
        var gate: CompletableDeferred<Unit>? = null
        override suspend fun login(userId: String, password: String): ApiResult<UserSession> {
            calls++
            credentials = userId to password
            gate?.await()
            return result
        }
        override suspend fun logout(): ApiResult<Unit> = ApiResult.Success(Unit)
    }

    @Test fun blankCredentialsAreRejectedWithoutCallingApi() = runTest {
        val auth = Auth()
        val vm = LoginViewModel(auth)
        vm.login("  ", "password")
        assertTrue(vm.state.value.invalidInput)
        vm.login("test", " ")
        advanceUntilIdle()
        assertTrue(vm.state.value.invalidInput)
        assertEquals(0, auth.calls)
    }

    @Test fun successfulLoginTrimsIdButPreservesPasswordAndDoesNotRetainItInUiState() = runTest {
        val auth = Auth()
        val vm = LoginViewModel(auth)
        vm.login(" test ", " password ")
        assertTrue(vm.state.value.loading)
        advanceUntilIdle()
        assertEquals("test" to " password ", auth.credentials)
        assertTrue(vm.state.value.signedIn)
        assertFalse(vm.state.value.loading)
        assertFalse(vm.state.value.toString().contains("password"))
    }

    @Test fun repeatedSubmitDuringRequestOnlyCallsApiOnce() = runTest {
        val auth = Auth().apply { gate = CompletableDeferred() }
        val vm = LoginViewModel(auth)
        vm.login("test", "password")
        runCurrent()
        vm.login("test", "password")
        assertEquals(1, auth.calls)
        auth.gate!!.complete(Unit)
        advanceUntilIdle()
        assertTrue(vm.state.value.signedIn)
    }

    @Test fun failureAllowsRetryAndClearsOldErrorOnSuccess() = runTest {
        val auth = Auth().apply { result = ApiResult.Failure(ApiError.INVALID_CREDENTIALS) }
        val vm = LoginViewModel(auth)
        vm.login("test", "wrong")
        advanceUntilIdle()
        assertEquals(ApiError.INVALID_CREDENTIALS, vm.state.value.error)
        assertFalse(vm.state.value.signedIn)
        auth.result = ApiResult.Success(UserSession("test", "Test", "test-token"))
        vm.login("test", "correct")
        advanceUntilIdle()
        assertTrue(vm.state.value.signedIn)
        assertNull(vm.state.value.error)
        assertEquals(2, auth.calls)
    }
}
