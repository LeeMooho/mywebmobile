package com.example.myweb.api

import com.example.myweb.model.BoardDTO
import com.example.myweb.model.ResponseDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BoardApi {
    @GET("/api/board/board-list")
    suspend fun getBoardList(
        @Query("searchCondition") searchCondition: String,
        @Query("searchKeyword") searchKeyword: String
    ): Response<ResponseDTO<BoardDTO>>
}