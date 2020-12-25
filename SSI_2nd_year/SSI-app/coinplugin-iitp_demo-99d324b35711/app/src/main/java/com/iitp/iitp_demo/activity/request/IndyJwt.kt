package com.iitp.iitp_demo.activity.request

data class IndyHeader(
        var kid: String,
        var alg: String
)

data class IndyPayload(
        var sub: String,
        var iss: String,
        var claim: Map<String, Object>,
        var type: ArrayList<String>,
        var version: String,
        var nonce: String,
        var iat: Long
)


