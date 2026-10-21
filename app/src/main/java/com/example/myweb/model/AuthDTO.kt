package com.example.myweb.model

// Field names follow LeeMooho/myweb's UserController and UserDTO.
class LoginRequest(val userId: String, val userPw: String) {
    override fun toString() = "LoginRequest(userId=<redacted>, userPw=<redacted>)"
}

class AuthUserDTO(val userId: String?, val userName: String?, val token: String?) {
    override fun toString() = "AuthUserDTO(<redacted>)"
}
