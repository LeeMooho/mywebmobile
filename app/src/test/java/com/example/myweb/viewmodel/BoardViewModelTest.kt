package com.example.myweb.viewmodel

import com.example.myweb.api.ApiError
import com.example.myweb.api.ApiResult
import com.example.myweb.data.BoardRepository
import com.example.myweb.model.BoardDTO
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BoardViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    @Test fun loadMovesFromLoadingToReady() = runTest {
        val board = BoardDTO(1, "Title", "Content", "2026-10-22", "Writer")
        val vm = BoardViewModel(BoardRepository { ApiResult.Success(listOf(board)) })
        vm.fetchBoards()
        assertEquals(BoardUiState.Loading, vm.state.value)
        advanceUntilIdle()
        assertEquals(BoardUiState.Ready(listOf(board)), vm.state.value)
    }

    @Test fun failureCanBeRetriedAndEmptySuccessIsDistinct() = runTest {
        var result: ApiResult<List<BoardDTO>> = ApiResult.Failure(ApiError.NETWORK)
        val vm = BoardViewModel(BoardRepository { result })
        vm.fetchBoards()
        advanceUntilIdle()
        assertEquals(BoardUiState.Error(ApiError.NETWORK), vm.state.value)
        result = ApiResult.Success(emptyList())
        vm.fetchBoards()
        assertEquals(BoardUiState.Loading, vm.state.value)
        advanceUntilIdle()
        assertEquals(BoardUiState.Ready(emptyList()), vm.state.value)
    }

    @Test fun refreshWhileLoadingDoesNotDuplicateRequest() = runTest {
        val gate = CompletableDeferred<Unit>()
        var calls = 0
        val vm = BoardViewModel(BoardRepository {
            calls++
            gate.await()
            ApiResult.Success(emptyList())
        })
        vm.fetchBoards()
        runCurrent()
        vm.fetchBoards()
        assertEquals(1, calls)
        gate.complete(Unit)
        advanceUntilIdle()
        assertEquals(BoardUiState.Ready(emptyList()), vm.state.value)
    }
}
