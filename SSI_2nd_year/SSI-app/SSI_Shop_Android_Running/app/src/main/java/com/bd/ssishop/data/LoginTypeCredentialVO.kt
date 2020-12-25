package com.bd.ssishop.data


import com.google.gson.annotations.SerializedName

data class LoginTypeCredentialVO(
    val claim: Claim,
    val exp: Int,
    val iat: Int,
    val iss: String,
    val nonce: String,
    val sub: String,
    val type: List<String>,
    val version: String
)