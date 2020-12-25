package com.bd.ssishop.login

/**
 * 사용자 정보 데이터 클래스
 */
data class User (
    val phone_id: String,
    val did: String,
    val username: String,
    val password: String,
    val name: String,
    val phone: String,
    val address: String,
    val role: String,
    val birthDate: String
)