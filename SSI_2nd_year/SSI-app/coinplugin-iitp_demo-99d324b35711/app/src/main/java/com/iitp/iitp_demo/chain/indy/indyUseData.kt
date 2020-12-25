package com.iitp.iitp_demo.chain.indy

import com.google.gson.annotations.SerializedName
import com.iitp.iitp_demo.util.PrintLog
import java.math.BigInteger

data class WalletCredentailVo(
        @SerializedName("key") var key: String
)

data class WalletConfigVo(
        @SerializedName("id") var id: String,
        @SerializedName("storage_type") var storage_type: String
)

data class CredentialValuesVo(
        @SerializedName("id") var id: HashMap<String, CredentialDataVo>
)

data class CredentialConfigVo(
        @SerializedName("support_revocation") var support_revocation: Boolean
)

data class ZkpClaimVo(
        @SerializedName("sub") var sub: String,
        @SerializedName("iss") var iss: String,
@SerializedName("claim") var claim: String,
@SerializedName("nonce") var nonce: String
)

class CredentialDataVo {

    @SerializedName("raw")
    var id: String
    @SerializedName("encoded")
    lateinit var encoded: String

    constructor(id: String) {
        this.id = id
        this.encoded = encodeRaw(id)
    }

    //https://github.com/PSPC-SPAC-buyandsell/von_agent/blob/master/von_agent/codec.py
    fun encodeRaw(arg: String): String {
        val a = String.format("%2x", BigInteger(1, arg.toByteArray()))
//        PrintLog.e("1  = $a")
        val b = BigInteger(a, 16)
//        PrintLog.e("2  = $b")
        return b.toString()
    }

    fun decodeRaw(arg: String?): String {
        val aa = BigInteger(arg)
        PrintLog.e("1  = $aa")
        val bString = aa.toString(16)
        PrintLog.e("2  = $bString")
        val output = StringBuilder()
        var i = 0
        while (i < bString.length) {
            val str = bString.substring(i, i + 2)
            output.append(str.toInt(16).toChar())
            i += 2
        }
        PrintLog.e("3  = $output")
        return output.toString()
    }
}




