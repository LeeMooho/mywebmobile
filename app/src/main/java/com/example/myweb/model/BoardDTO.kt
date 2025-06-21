package com.example.myweb.model

data class BoardDTO(
    val boardNo: Long,
    val boardTitle: String,
    val boardContent: String,
    val boardRegdate: String,
    val boardWriter: String
)

data class ResponseDTO<T>(
    val item: T?,
    val pageItems: Page<T>?,
    val statusCode: Int,
    val errorCode: Int?,
    val errorMessage: String?
)

data class Page<T>(
    val content: List<T>,
    val totalPages: Int,
    val totalElements: Int,
    val number: Int
)
