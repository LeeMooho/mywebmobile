package com.example.myweb.api

import com.example.myweb.BuildConfig
import com.example.myweb.data.SessionStore
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class SessionInterceptor(private val baseUrl: HttpUrl, private val sessions: SessionStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val sameOrigin = request.url.scheme == baseUrl.scheme && request.url.host == baseUrl.host &&
            request.url.port == baseUrl.port
        val token = sessions.session.value?.token?.takeIf {
            sameOrigin && request.url.isHttps && !request.url.encodedPath.endsWith("/user/login")
        }
        val authenticated = request.newBuilder().apply {
            if (token != null) header("Authorization", "Bearer $token")
        }.build()
        val response = chain.proceed(authenticated)
        if (response.code == 401 && token != null) sessions.clearIfTokenMatches(token)
        return response
    }
}

class ApiClient(
    sessions: SessionStore,
    baseUrl: String = BuildConfig.API_BASE_URL,
    client: OkHttpClient = OkHttpClient()
) {
    val url = baseUrl.toHttpUrl().also {
        require(it.encodedPath.endsWith("/")) { "API base URL must end in /" }
        require(it.username.isEmpty() && it.password.isEmpty() && it.query == null && it.fragment == null)
    }
    private val retrofit = Retrofit.Builder().baseUrl(url)
        .client(client.newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .followRedirects(false).followSslRedirects(false)
            .addInterceptor(SessionInterceptor(url, sessions)).build())
        .addConverterFactory(GsonConverterFactory.create()).build()
    val boards: BoardApi = retrofit.create(BoardApi::class.java)
    val auth: AuthApi = retrofit.create(AuthApi::class.java)
}
