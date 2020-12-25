package com.iitp.iitp_demo.activity.model

import com.iitp.verifiable.VerifiableCredential
import java.io.Serializable

data class CredentialListVo(
        var imageIcon: Int,
        var desc1: String,
        var desc2: String,
        var did: String?,
        var index : Int
)

data class CredentialData(
        var credential: VerifiableCredential,
        var desc1: String?,
        var did: String?,
        var type: String?
) :Serializable

data class RequestCredentialData(
        var name: String?,
        var data: String,
        var did: String
)




