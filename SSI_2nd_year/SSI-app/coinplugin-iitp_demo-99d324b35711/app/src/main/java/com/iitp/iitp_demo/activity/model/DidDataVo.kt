package com.iitp.iitp_demo.activity.model

import com.google.gson.reflect.TypeToken
import com.iitp.iitp_demo.Constants
import com.iitp.iitp_demo.IITPApplication
import com.iitp.iitp_demo.util.CommonPreference
import com.iitp.iitp_demo.util.PrintLog
import java.util.*

enum class BlockChainType {
    METADIUM, INDY, ICON
}

data class DidDataVo(
        var did: String,
        var privateKey: String,
        var publicKey: String,
        var create_at: Long,
        var nickName: String,
        var blackChain: BlockChainType,
        var favorite: Boolean,
        var iconKeyId: String?,
        var mnemonic: String?
)
data class ProductVC(
        var productVC: String,
        var productProofVC: String,
        var walletJson: String?

)


