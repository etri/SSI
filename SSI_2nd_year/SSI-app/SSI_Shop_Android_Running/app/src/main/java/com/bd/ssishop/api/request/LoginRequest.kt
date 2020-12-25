package com.bd.ssishop.api.request

/**
 * login API Request 객체
 */
data class LoginRequest(
    val username: String?,
    val password: String?
)