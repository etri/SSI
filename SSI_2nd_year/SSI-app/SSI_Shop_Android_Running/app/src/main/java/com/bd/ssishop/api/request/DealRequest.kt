package com.bd.ssishop.api.request

/**
 * 거래 API Request 객체
 */
data class DealRequest(val dealId: Int) {
    lateinit var did: String
    lateinit var didLogin: String
    lateinit var publicKey: String
    lateinit var sign: String
    lateinit var didBuyer: String

    override fun toString(): String {
        return "DealRequest(dealId=$dealId, did='$did', didLogin='$didLogin', publicKey='$publicKey', sign='$sign', didBuyer='$didBuyer')"
    }
}