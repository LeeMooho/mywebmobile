package com.example.myweb.api

import com.example.myweb.model.AuthUserDTO
import com.example.myweb.model.LoginRequest
import com.example.myweb.model.ResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("api/user/login")
    suspend fun login(@Body request: LoginRequest): Response<ResponseDTO<AuthUserDTO>>

    @GET("api/user/logout")
    suspend fun logout(): Response<ResponseDTO<Map<String, String>>>
}
