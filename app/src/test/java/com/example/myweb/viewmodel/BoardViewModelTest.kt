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

    private class FakeBoardRepository(
        var listResult: ApiResult<List<BoardDTO>> = ApiResult.Success(emptyList()),
        var detailResult: ApiResult<BoardDTO> = ApiResult.Failure(ApiError.INVALID_RESPONSE),
        val beforeList: suspend () -> Unit = {},
        val beforeDetail: suspend () -> Unit = {}
    ) : BoardRepository {
        var listCalls = 0
        var detailCalls = 0
        var requestedBoardNo: Long? = null

        override suspend fun loadBoards(): ApiResult<List<BoardDTO>> {
            listCalls++
            beforeList()
            return listResult
        }

        override suspend fun loadBoard(boardNo: Long): ApiResult<BoardDTO> {
            detailCalls++
            requestedBoardNo = boardNo
            beforeDetail()
            return detailResult
        }
    }

    @Test fun loadMovesFromLoadingToReady() = runTest {
        val board = BoardDTO(1, "Title", "Content", "2026-10-22", "Writer")
        val vm = BoardViewModel(FakeBoardRepository(listResult = ApiResult.Success(listOf(board))))
        vm.fetchBoards()
        assertEquals(BoardUiState.Loading, vm.state.value)
        advanceUntilIdle()
        assertEquals(BoardUiState.Ready(listOf(board)), vm.state.value)
    }

    @Test fun failureCanBeRetriedAndEmptySuccessIsDistinct() = runTest {
        val repository = FakeBoardRepository(listResult = ApiResult.Failure(ApiError.NETWORK))
        val vm = BoardViewModel(repository)
        vm.fetchBoards()
        advanceUntilIdle()
        assertEquals(BoardUiState.Error(ApiError.NETWORK), vm.state.value)
        repository.listResult = ApiResult.Success(emptyList())
        vm.fetchBoards()
        assertEquals(BoardUiState.Loading, vm.state.value)
        advanceUntilIdle()
        assertEquals(BoardUiState.Ready(emptyList()), vm.state.value)
    }

    @Test fun refreshWhileLoadingDoesNotDuplicateRequest() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeBoardRepository(beforeList = { gate.await() })
        val vm = BoardViewModel(repository)
        vm.fetchBoards()
        runCurrent()
        vm.fetchBoards()
        assertEquals(1, repository.listCalls)
        gate.complete(Unit)
        advanceUntilIdle()
        assertEquals(BoardUiState.Ready(emptyList()), vm.state.value)
    }

    @Test fun detailLoadUsesRequestedIdentifierAndMovesToReady() = runTest {
        val board = BoardDTO(42, "Title", "Full content", "2026-10-22", "Writer")
        val repository = FakeBoardRepository(detailResult = ApiResult.Success(board))
        val vm = BoardDetailViewModel(42, repository)
        vm.fetchBoard()
        assertEquals(BoardDetailUiState.Loading, vm.state.value)
        advanceUntilIdle()
        assertEquals(42L, repository.requestedBoardNo)
        assertEquals(BoardDetailUiState.Ready(board), vm.state.value)
    }

    @Test fun detailFailureCanBeRetried() = runTest {
        val board = BoardDTO(42, "Title", "Full content", "2026-10-22", "Writer")
        val repository = FakeBoardRepository(detailResult = ApiResult.Failure(ApiError.NETWORK))
        val vm = BoardDetailViewModel(42, repository)
        vm.fetchBoard()
        advanceUntilIdle()
        assertEquals(BoardDetailUiState.Error(ApiError.NETWORK), vm.state.value)
        repository.detailResult = ApiResult.Success(board)
        vm.fetchBoard()
        advanceUntilIdle()
        assertEquals(BoardDetailUiState.Ready(board), vm.state.value)
        assertEquals(2, repository.detailCalls)
    }

    @Test fun repeatedDetailLoadDuringRequestOnlyCallsRepositoryOnce() = runTest {
        val gate = CompletableDeferred<Unit>()
        val board = BoardDTO(42, "Title", "Full content", "2026-10-22", "Writer")
        val repository = FakeBoardRepository(
            detailResult = ApiResult.Success(board),
            beforeDetail = { gate.await() }
        )
        val vm = BoardDetailViewModel(42, repository)
        vm.fetchBoard()
        runCurrent()
        vm.fetchBoard()
        assertEquals(1, repository.detailCalls)
        gate.complete(Unit)
        advanceUntilIdle()
        assertEquals(BoardDetailUiState.Ready(board), vm.state.value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonPositiveDetailIdentifierIsRejected() {
        BoardDetailViewModel(0, FakeBoardRepository())
    }
}
