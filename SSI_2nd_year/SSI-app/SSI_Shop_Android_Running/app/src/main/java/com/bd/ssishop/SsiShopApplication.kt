package com.bd.ssishop

import android.app.Application
import android.content.Context
import android.provider.Settings
import com.bd.ssishop.login.User

/**
 * 전역변수용 클래스
 */
class SsiShopApplication: Application() {
    companion object {
        lateinit var token: String
        lateinit var user: User
        val uuid: String = java.util.UUID.randomUUID().toString()
        lateinit var pid: String

        lateinit var typedName: String
        lateinit var typedPhone: String
        lateinit var typedZipcode: String
        lateinit var typedAddress: String
        lateinit var paymentMethod: String
        lateinit var paymentCard: String

        lateinit var usedProductId: String
        lateinit var usedBuyerId: String
        lateinit var usedDealId: String
        lateinit var dealId: String
        lateinit var productType: String

        var parsedDealId: String? = ""

        lateinit var didSeller: String
        lateinit var didSelected: String
        lateinit var didLogin: String

        var runMode:String = ""
    }
}