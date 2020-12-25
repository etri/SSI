package com.iitp.iitp_demo.api.model

import com.google.gson.annotations.SerializedName

data class AuthIdVo(
        @SerializedName("authId") var authId: String
)

data class AuthResponseVo(
        @SerializedName("success") var success: Boolean,
        @SerializedName("result") var result: AuthInitResponseResultVo
)
data class AuthInitResponseResultVo(
        @SerializedName("verifyToken") var verifyToken: String,
        @SerializedName("authToken") var authToken: String,
        @SerializedName("recoveryKey") var recoveryKey: String,
        @SerializedName("recoveryClue") var recoveryClue: String,
        @SerializedName("dataClue") var dataClue: String
)


data class AuthVerifyVo(
        @SerializedName("verifyToken") var verifyToken: String,
        @SerializedName("authCode") var authCode: String
)
data class AuthBackupVo(
        @SerializedName("authToken") var authToken: String,
        @SerializedName("recoveryKey") var recoveryKey: String
)

data class AuthBackupDataVo(
        @SerializedName("authToken") var authToken: String,
        @SerializedName("recoveryClue") var recoveryClue: String,
        @SerializedName("dataClue") var dataClue: String
)

data class AuthRecoveryDataVo(
        @SerializedName("authToken") var authToken: String
)









