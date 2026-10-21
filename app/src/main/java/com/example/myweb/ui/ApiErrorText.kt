package com.example.myweb.ui

import com.example.myweb.R
import com.example.myweb.api.ApiError

fun ApiError.messageResource(): Int = when (this) {
    ApiError.NETWORK -> R.string.error_network
    ApiError.UNAUTHORIZED -> R.string.error_unauthorized
    ApiError.INVALID_CREDENTIALS -> R.string.error_credentials
    ApiError.SERVER -> R.string.error_server
    ApiError.INVALID_RESPONSE -> R.string.error_response
    ApiError.SECURE_CONNECTION_REQUIRED -> R.string.error_secure_connection
}
