package com.bd.ssishop.data

data class VCsTemplate (val paramMap:MutableMap<String, String>){
    val sendVcsForLogin : String =
            "{" +
                "iss\": \"${paramMap.get("ISSUER_DID")}\"," +
                "\"iat\": \"${paramMap.get("ISSUE_TIME")}\"," +
                "\"id\": \"${paramMap.get("UUID")}\"," +
                "\"presentationURL\": \"${paramMap.get("URL")}\"," +
                "\"presentationRequest\": {" +
                    "\"criteria\": [{" +
                        "\"nonZKP\": {" +
                            "\"nonce\": \"123432421212\"," +
                            "\"name\": \"login-Request\"," +
                            "\"version\": \"0.1\"," +
                            "\"requested_attributes\": {" +
                                "\"attr1_referent\": {\"restrictions\": [{\"issuer\": \"${paramMap.get("ISSUER_DID")}\"}, {\"type\": \"LoginCredential\"}]}" +
                            "}" +
                        "}" +
                    "}]" +
                "}" +
            "}"


    val sendVcsForBuying: String =
        "{" +
            "\"iss\":\"${paramMap.get("ISSUER_DID")}\"," +
            "\"iat\":\"${paramMap.get("ISSUE_TIME")}\"," +
            "\"id\":\"${paramMap.get("UUID")}\"," +
            "\"presentationURL\":\"${paramMap.get("URL")}\"," +
            "\"presentationRequest\":{" +
                "\"criteria\":[" +
                    "{\"nonZKP\":" +
                        "{\"nonce\":\"123432421212\"," +
                         "\"name\":\"Market-Payment-Request\"," +
                         "\"version\":\"0.1\"," +
                         "\"delegated_attributes\":" +
                            "{\"delegated_attr1_referent\":" +
                                "{\"type\":\"delegated_VC\"," +
                                 "\"delegated_attr\":\"‘CardTokenCredential’\"," +
                                 "\"did_delegator\":\"${paramMap.get("ISSUER_DID")}\"," +
                                 "\"payment\":\"${paramMap.get("payment")}\"" +
                                "}" +
                            "}," +
                         "\"requested_attributes\":" +
                            "{\"attr1_referent\":" +
                                "{\"restrictions\":" +
                                    "[" +
                                        "{\"issuer\":\"${paramMap.get("CARD_ISSUER_DID")}\"}," +
                                        "{\"type\":\"CardTokenCredential\"}" +
                                    "]" +
                                "}," +
                             "\"attr2_referent\":" +
                                "{\"restrictions\":" +
                                    "[" +
                                        "{\"issuer\":\"${paramMap.get("ISSUER_DID")}\"}," +
                                        "{\"type\":\"PhoneCredential\"}" +
                                    "]" +
                                "}," +
                             "\"attr3_referent\":" +
                                "{\"restrictions\":" +
                                    "[" +
                                        "{\"issuer\":\"${paramMap.get("ISSUER_DID")}\"}," +
                                        "{\"type\":\"AddressCredential\"}" +
                                    "]" +
                                "}" +
                            "}" +
                        "}" +
                    "}" +
                "]" +
            "}" +
        "}"

    val reqVpForUsedRegi: String =
        "{" +
            "\"iss\":\"${paramMap.get("ISSUER_DID")}\"," +
            "\"iat\":\"${paramMap.get("ISSUE_TIME")}\"," +
            "\"id\":\"${paramMap.get("UUID")}\",\n" +
            "\"presentationURL\":\"${paramMap.get("URL")}\"," +
            "\"presentationRequest\":{" +
                "\"criteria\":" +
                    "[" +
                        "{\"nonZKP\":{" +
                            "\"nonce\":\"123432421212\"," +
                            "\"name\":\"Product-Description-Post-Request\"," +
                            "\"version\":\"0.1\"," +
                            "\"requested_attributes\":{" +
                                "\"attr1_referent\":{" +
                                    "\"restrictions\":" +
                                        "[" +
                                            "{\"issuer\":\"${paramMap.get("ISSUER_DID")}\"}," +
                                            "{\"type\":\"ProductCredential\"}" +
                                        "]" +
                                "}," +
                                "\"attr2_referent\":{" +
                                    "\"restrictions\":" +
                                        "[" +
                                            "{\"issuer\":\"${paramMap.get("ISSUER_DID")}\"}," +
                                            "{\"type\":\"ProductProofCredential\"}" +
                                        "]" +
                                    "}" +
                                "}" +
                            "}" +
                        "}" +
                    "]" +
            "}" +
        "}"

    @JvmName("getSendVcsForLogin1")
    fun getSendVcsForLogin(): String{
        return sendVcsForLogin
    }

    @JvmName("getSendVcsForBuying1")
    fun getSendVcsForBuying(): String{
        return sendVcsForBuying
    }

    @JvmName("getReqVpForUsedRegi1")
    fun getReqVpForUsedRegi(): String{
        return reqVpForUsedRegi
    }
 }