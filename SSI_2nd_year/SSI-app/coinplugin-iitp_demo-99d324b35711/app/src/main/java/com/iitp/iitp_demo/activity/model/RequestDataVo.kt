package com.iitp.iitp_demo.activity.model

import com.google.gson.annotations.SerializedName
import org.web3j.abi.datatypes.Bool

enum class appType {
    auth, univ, company
}

data class RequestDataVo(
        @SerializedName("msgid") var msgid: MsgidVo,
        @SerializedName("func") var func: String,
        @SerializedName("data") var data: DataVo?

)

data class MsgidVo(
        @SerializedName("service") var service: String,
        @SerializedName("func") var func: String,
        @SerializedName("action") var action: String

)

data class DataVo(
        @SerializedName("url") var url: String?,
        @SerializedName("result") var result: Boolean,
        @SerializedName("did") var did: String?,
        @SerializedName("issuer") var issuer: String?,
        @SerializedName("jwt") var jwt: String?,
        @SerializedName("name") var name: String?,
        @SerializedName("birth") var birth: String?,
        @SerializedName("address") var address: String?,
        @SerializedName("idcard") var idCard: String?,
        @SerializedName("exist") var exist: Boolean

)

data class RequestJobDataVo(
        @SerializedName("msgid") var msgid: MsgidVo,
        @SerializedName("func") var func: String,
        @SerializedName("data") var data: JobDataVo?

)
data class JobDataVo(
        @SerializedName("vc") var vc: String,
        @SerializedName("result") var result: Boolean,
        @SerializedName("offer") var offer: String?,
        @SerializedName("schemaid") var schemaid: String?,
        @SerializedName("credid") var credid: String?,
        @SerializedName("requestapi") var requestapi: String?,
        @SerializedName("id") var id: String?,
        @SerializedName("platform") var platform: String?,
        @SerializedName("jwt") var jwt: String?


)

