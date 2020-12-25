package com.bd.ssishop.api

/**
 * Api 공통 응답 객체 생성
 */
data class ApiResponse<T>(
    val code: Int,
    val data: T? = null,
    val message: String? = null
) {
    companion object {
        inline fun <reified T> error(code: Int, message: String? = null) = ApiResponse(code, null as T?, message )
    }
}