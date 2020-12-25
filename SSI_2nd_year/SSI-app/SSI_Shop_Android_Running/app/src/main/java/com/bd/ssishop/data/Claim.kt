package com.bd.ssishop.data


import com.google.gson.annotations.SerializedName

data class Claim(
    val address: String,
    @SerializedName("birth_date")
    val birthDate: String,
    val name: String,
    var phone_num: String,
    val phone: String
)