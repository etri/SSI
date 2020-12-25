package com.bd.ssishop.data


import com.google.gson.annotations.SerializedName

data class LoginTypeVO(
    val credential: List<String>,
    val iss: String,
    val nonce: String,
    val type: List<String>,
    val version: String
)