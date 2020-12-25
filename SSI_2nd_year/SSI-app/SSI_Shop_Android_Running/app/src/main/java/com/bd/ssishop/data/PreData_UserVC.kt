package com.bd.ssishop.data

data class PreData_UserVC(val initVal:String?) {
    val PHONE_ID: String? = initVal         //device uuid

    val ISSUER_DID: String = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001bd1"
    val ISSUER_KID: String  = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001bd1#MetaManagementKey#bae67d929f5758cceb8e43cdc6056eff7bbe4f92";
    val USER_DID: String = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001bd4"

    val name : String = "hong gil dong"
    val addr : String = "218 Gajeong-ro, Yuseong-gu, Daejeon, 34129, KOREA"
    val birth : String = "1988-07-21"
    val payment : Int = 5000
}