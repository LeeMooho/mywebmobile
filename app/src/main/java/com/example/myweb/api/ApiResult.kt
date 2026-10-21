package com.example.myweb.api

import com.example.myweb.model.ResponseDTO
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.stream.MalformedJsonException
import java.io.EOFException
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException

enum class ApiError { NETWORK, UNAUTHORIZED, INVALID_CREDENTIALS, SERVER, INVALID_RESPONSE, SECURE_CONNECTION_REQUIRED }

sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>
    data class Failure(val error: ApiError, val httpCode: Int? = null) : ApiResult<Nothing>
}

private fun failure(status: Int, backendCode: Int? = null): ApiResult.Failure {
    val error = when {
        status == 401 || status == 403 -> ApiError.UNAUTHORIZED
        backendCode == 200 || backendCode == 201 -> ApiError.INVALID_CREDENTIALS
        else -> ApiError.SERVER
    }
    return ApiResult.Failure(error, status)
}

suspend fun <T> apiCall(call: suspend () -> Response<ResponseDTO<T>>): ApiResult<ResponseDTO<T>> = try {
    val response = call()
    if (!response.isSuccessful) {
        val errorCode = response.errorBody()?.use { body ->
            try {
                JsonParser().parse(body.charStream()).asJsonObject.get("errorCode")?.asInt
            } catch (_: RuntimeException) { null }
        }
        failure(response.code(), errorCode)
    } else {
        val body = response.body()
        when {
            body == null || body.statusCode == 0 -> ApiResult.Failure(ApiError.INVALID_RESPONSE)
            (body.errorCode != null && body.errorCode != 0) || body.statusCode !in 200..299 -> failure(body.statusCode, body.errorCode)
            else -> ApiResult.Success(body)
        }
    }
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (_: JsonParseException) {
    ApiResult.Failure(ApiError.INVALID_RESPONSE)
} catch (_: MalformedJsonException) {
    ApiResult.Failure(ApiError.INVALID_RESPONSE)
} catch (_: EOFException) {
    ApiResult.Failure(ApiError.INVALID_RESPONSE)
} catch (_: IOException) {
    ApiResult.Failure(ApiError.NETWORK)
}
