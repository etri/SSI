package com.iitp.iitp_demo.api.model

import com.google.gson.annotations.SerializedName

data class ChallengeVo(
        @SerializedName("did") var did: String,
        @SerializedName("signature") var signature: String
)

data class WebResultVo(
        @SerializedName("result") var result: Boolean,
        @SerializedName("offer") var offer: String
)












