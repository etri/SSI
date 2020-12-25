package com.bd.ssishop.market.deal

import com.bd.ssishop.login.User
import com.bd.ssishop.market.product.Product

/**
 * 구매내역 데이터 클래스
 */
data class Deal (
    val dealId: Int,
    val product: Product,
    val buyer: User,
    val buyerName: String,
    val paymentMethod: String,
    val state: String,
    val phone: String,
    val address: String,
    val count: Int,
    val dealDate: String,
    val did: String,
    val publicKey: String,
    val didSelected: String,
    val didBuyer: String,
    val didSeller: String,
    val sign: String,
    val vcState: String,
    var etc:String?
)