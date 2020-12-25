package com.bd.ssishop.api.request

/**
 * 구매 API Request 객체
 */
data class PurchaseRequest(
    val vp: String,
    val productId: Int,
    val paymentMethod: String,
    val count: Int,
    val phone: String,
    val address: String
)