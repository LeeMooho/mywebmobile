package com.example.myweb.data

import com.example.myweb.api.ApiError
import com.example.myweb.api.ApiResult
import com.example.myweb.api.BoardApi
import com.example.myweb.api.apiCall
import com.example.myweb.model.BoardDTO

fun interface BoardRepository {
    suspend fun loadBoards(): ApiResult<List<BoardDTO>>
}

class RemoteBoardRepository(private val api: BoardApi) : BoardRepository {
    override suspend fun loadBoards(): ApiResult<List<BoardDTO>> = when (
        val result = apiCall { api.getBoardList("all", "") }
    ) {
        is ApiResult.Failure -> result
        is ApiResult.Success -> result.value.pageItems?.content?.let { ApiResult.Success(it) }
            ?: ApiResult.Failure(ApiError.INVALID_RESPONSE)
    }
}
