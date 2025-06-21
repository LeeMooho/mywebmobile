package com.example.myweb.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    val api: BoardApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://www.mooho.shop:8080/") // 또는 도메인 주소
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BoardApi::class.java)
    }
}