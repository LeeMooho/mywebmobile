package com.example.myweb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myweb.api.ApiError
import com.example.myweb.api.ApiResult
import com.example.myweb.data.BoardRepository
import com.example.myweb.model.BoardDTO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface BoardUiState {
    object Loading : BoardUiState
    data class Ready(val boards: List<BoardDTO>) : BoardUiState
    data class Error(val error: ApiError) : BoardUiState
}

class BoardViewModel(private val repository: BoardRepository) : ViewModel() {
    private val mutableState = MutableStateFlow<BoardUiState>(BoardUiState.Loading)
    val state = mutableState.asStateFlow()
    private var request: Job? = null

    fun fetchBoards() {
        if (request?.isActive == true) return
        mutableState.value = BoardUiState.Loading
        request = viewModelScope.launch {
            mutableState.value = when (val result = repository.loadBoards()) {
                is ApiResult.Success -> BoardUiState.Ready(result.value)
                is ApiResult.Failure -> BoardUiState.Error(result.error)
            }
        }
    }
}

sealed interface BoardDetailUiState {
    object Loading : BoardDetailUiState
    data class Ready(val board: BoardDTO) : BoardDetailUiState
    data class Error(val error: ApiError) : BoardDetailUiState
}

class BoardDetailViewModel(
    private val boardNo: Long,
    private val repository: BoardRepository
) : ViewModel() {
    init {
        require(boardNo > 0) { "boardNo must be positive" }
    }

    private val mutableState = MutableStateFlow<BoardDetailUiState>(BoardDetailUiState.Loading)
    val state = mutableState.asStateFlow()
    private var request: Job? = null

    fun fetchBoard() {
        if (request?.isActive == true) return
        mutableState.value = BoardDetailUiState.Loading
        request = viewModelScope.launch {
            mutableState.value = when (val result = repository.loadBoard(boardNo)) {
                is ApiResult.Success -> BoardDetailUiState.Ready(result.value)
                is ApiResult.Failure -> BoardDetailUiState.Error(result.error)
            }
        }
    }
}
