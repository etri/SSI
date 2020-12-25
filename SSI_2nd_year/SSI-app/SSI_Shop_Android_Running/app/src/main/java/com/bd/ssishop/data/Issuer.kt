package com.bd.ssishop.data

data class Issuer(val init:String?){
    companion object {
        val ISSUER_DID: String = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001de8"
        val ISSUER_KID: String = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001de8#MetaManagementKey#fcc7a2d69c88a6a615e59717c5b9c120c1b04bb6"
        val ISSUER_PRIVATEKEY: String = "4bffb79cf64f3b4e1e6bf91390cd049141269b856c763dd7dad6c714ed251c85"
        val CARD_ISSUER_DID_01 = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001d4f"    //한국카드. 2020.11.23 임시값. 젠토에서 받은 값으로 변경해야 함
        val CARD_ISSUER_DID_02 = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001d5f"    //서울카드. 2020.11.23 임시값. 젠토에서 받은 값으로 변경해야 함
    }
}