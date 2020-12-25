package com.iitp.iitp_demo.api.model

import com.google.gson.annotations.SerializedName
import org.web3j.abi.datatypes.Bool

data class pushRegisterVo(
        @SerializedName("holder") var holderDid: String,
        @SerializedName("msToken") var token: String?
)

data class pushResponseVo(
        @SerializedName("status") var status: Boolean,
        @SerializedName("errorMessage") var errorMessage: String,
        @SerializedName("friends") var friends: List<pushFriendVo>,
        @SerializedName("holderDid") var holderDid: String,
        @SerializedName("holderVc") var holderVc: String,
        @SerializedName("poaVc") var poaVc: String
)


data class pushFriendVo(
        @SerializedName("did") var did: String,
        @SerializedName("name") var friends: String,
        @SerializedName("token") var token: String?
)

data class delegaterVCVo(
        @SerializedName("holder") var holder: String,
        @SerializedName("delegated") var delegated: String,
        @SerializedName("holderVc") var holderVc: String,
        @SerializedName("posVc") var posVc: String,
        @SerializedName("poaVc") var poaVc: String?
)

data class pushTokenVo(
        @SerializedName("action") var action: String,
        @SerializedName("token") var token: String,
        @SerializedName("message") var message: MessageVo
)


data class MessageVo(
        @SerializedName("requestURL") var requestURL: String,
        @SerializedName("msgid") var msgid: String,
        @SerializedName("sign") var sign: String,
        @SerializedName("url") var url: String,
        @SerializedName("issuer") var issuer: String


)

data class SendDIDVo(
        @SerializedName("did") var did: String,
        @SerializedName("msgid") var msgid: String
)

data class DidvoResponse(
        @SerializedName("result") var result: Boolean,
        @SerializedName("data") var msgid: DidResponseDataVo,
        @SerializedName("msg") var msg: String
)

data class DidResponseDataVo(
        @SerializedName("vc") var vc: String,
        @SerializedName("offer") var offer: String,
        @SerializedName("result") var result: Boolean,
        @SerializedName("schemaid") var schemaid: String,
        @SerializedName("id") var id: String,
        @SerializedName("credid") var credid: String,
        @SerializedName("requestapi") var requestapi: String
)













