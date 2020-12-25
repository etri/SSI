package com.bd.ssishop.api.response

import com.bd.ssishop.login.User

/**
 * 로그인 응답 객체
 */
data class LoginResponse (
    val user: User,
    val token: String
)