package com.iitp.iitp_demo.activity.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class VpResponseVo(
        @SerializedName("id") var id: String,
        @SerializedName("presentationRequestId") var presentationRequestId: String,
        @SerializedName("Presentations") var Presentations: String,
        @SerializedName("fcmtoken") var fcmtoken: String?,
        @SerializedName("vcdid") var vcdid: String?
)

data class VpRequestVo(
        @SerializedName("iss") var iss: String,
        @SerializedName("iat") var iat: String,
        @SerializedName("id") var id: String,
        @SerializedName("presentationURL") var presentationURL: String,
        @SerializedName("presentationRequest") var presentationRequest: VpRequestDataVoList

)

data class VpRequestDataVoList(
        @SerializedName("criteria") var criteria: List<VpRequestDataVo>
)

data class VpRequestDataVo(
        @SerializedName("nonZKP") var nonZKP: VpRequestNoneZKPDataVo,
        @SerializedName("ZKP") var ZKP: VpRequestZKPDataVo,
        @SerializedName("zkp") var zkp: VpRequestZKPDataVo
)

data class VpRequestNoneZKPDataVo(
        @SerializedName("nonce") var nonce: String,
        @SerializedName("name") var name: String,
        @SerializedName("version") var version: String,
        @SerializedName("requested_attributes") var requested_attributes: Map<String?, VpRequestZKPReferentDataVo?>?,
        @SerializedName("delegated_attributes") var delegated_attributes: Map<String?, VpRequestZKPDelegateDataVo?>?
)

data class VpRequestZKPDataVo(
        @SerializedName("nonce") var nonce: String,
        @SerializedName("name") var name: String,
        @SerializedName("version") var version: String,
        @SerializedName("requested_attributes") var requested_attributes: Map<String?, VpRequestZKPReferentDataVo?>?,
        @SerializedName("requested_predicates") var requested_predicates: Map<String?, VpRequestZKPPriReferentDataVo?>?

)




data class VpRequestZKPDelegateDataVo(
        @SerializedName("type") var type: String,
        @SerializedName("delegated_attr") var delegated_attr: String,
        @SerializedName("did_delegator") var did_delegator: String,
        @SerializedName("payment") var payment: String


)

data class VpRequestZKPReferentDataVo(
        @SerializedName("name") var name: String,
        @SerializedName("restrictions") var restrictions: List<Map<String?, String?>>?

)

data class VpRequestZKPPriReferentDataVo(
        @SerializedName("name") var name: String,
        @SerializedName("p_value") var p_value: Int,
        @SerializedName("p_type") var p_type: String,
        @SerializedName("restrictions") var restrictions: List<Map<String?, String?>>?

)


data class VpRequestRestrictionsDataListDataVo(
        @SerializedName("cred_def_id") var cred_def_id: String

)

data class RequestWebViewInterfaceVo(
        @SerializedName("msgid") var msgid: String,
        @SerializedName("func") var func: String,
        @SerializedName("data") var data: String

)


data class VCListVo(
        @SerializedName("vc") var vc: List<String>

)

data class IndyCredentialVo(
        @SerializedName("referent") var  referent: String,
        @SerializedName("schema_id") var  schema_id: String,
        @SerializedName("cred_def_id") var  cred_def_id: String,
        @SerializedName("rev_reg_id") var  rev_reg_id: String?,
        @SerializedName("cred_rev_id") var  cred_rev_id: String?,
        @SerializedName("attrs") var  attr: Map<String, String>
)





