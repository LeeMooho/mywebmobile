package com.example.myweb.data

import com.example.myweb.api.ApiError
import com.example.myweb.api.ApiResult
import com.example.myweb.api.BoardApi
import com.example.myweb.api.apiCall
import com.example.myweb.model.BoardDTO

interface BoardRepository {
    suspend fun loadBoards(): ApiResult<List<BoardDTO>>
    suspend fun loadBoard(boardNo: Long): ApiResult<BoardDTO>
}

class RemoteBoardRepository(private val api: BoardApi) : BoardRepository {
    override suspend fun loadBoards(): ApiResult<List<BoardDTO>> = when (
        val result = apiCall { api.getBoardList("all", "") }
    ) {
        is ApiResult.Failure -> result
        is ApiResult.Success -> result.value.pageItems?.content?.let { ApiResult.Success(it) }
            ?: ApiResult.Failure(ApiError.INVALID_RESPONSE)
    }

    override suspend fun loadBoard(boardNo: Long): ApiResult<BoardDTO> {
        require(boardNo > 0) { "boardNo must be positive" }
        return when (val result = apiCall { api.getBoard(boardNo) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> result.value.item?.let { ApiResult.Success(it) }
                ?: ApiResult.Failure(ApiError.INVALID_RESPONSE)
        }
    }
}
