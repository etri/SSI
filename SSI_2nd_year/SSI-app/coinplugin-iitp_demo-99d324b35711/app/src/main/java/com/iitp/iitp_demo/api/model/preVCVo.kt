package com.iitp.iitp_demo.api.model

import com.google.gson.annotations.SerializedName

data class RequestVCVo(
        @SerializedName("did") var did: String?,
        @SerializedName("name") var name: String?,
        @SerializedName("birth_date") var birth_date: String?,
        @SerializedName("address") var address: String?,
        @SerializedName("phone_num") var phone_num: String?,
        @SerializedName("idcardnum") var idcardnum: String?,
        @SerializedName("register_id") var register_id: String?,
        @SerializedName("start_date") var start_date: String?,
        @SerializedName("number") var school_id: String?,
        @SerializedName("birth") var birth: String?,
        @SerializedName("company") var company: String?,
        @SerializedName("admission_date") var uniStartDate: String?
)

data class RequestGetOfferVo(
        @SerializedName("did") var did: String?,
        @SerializedName("name") var name: String?,
        @SerializedName("birth_date") var birth_date: String?,
        @SerializedName("address") var address: String?
)

data class ResponseVC(
        @SerializedName("vc") var vc: String
)

data class ZkpResponse(
        @SerializedName("result") var result: Boolean,
        @SerializedName("offer") var offer: String,
        @SerializedName("schemaid") var schemaid: String,
        @SerializedName("id") var id: String,
        @SerializedName("credid") var credid: String,
        @SerializedName("exist") var exist: Boolean,
        @SerializedName("requestapi") var requestapi: String
)

data class CredentialRequestVo(
        @SerializedName("id") var id: String,
        @SerializedName("request") var request: String
)

data class CredentialRequestResponseVo(
        @SerializedName("result") var result: String,
        @SerializedName("vc") var vc: String
)

data class StoreCredentialDataVo(
        @SerializedName("credReqMetadataJson") var credReqMetadataJson: String,
        @SerializedName("credentialOffer") var credentialOffer: String,
        @SerializedName("credDefJson") var credDefJson: String,
        @SerializedName("schemaJson") var schemaJson: String,
        @SerializedName("credentialDefId") var credentialDefId: String,
        @SerializedName("schemaId") var schemaId: String
)













