package com.iitp.core.protocol.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * registry address
 */
public class RegistryAddress {

    @JsonProperty("identity_registry")
    public String identityRegistry;

    @JsonProperty("providers")
    public List<String> providers;

    @JsonProperty("public_key")
    public String publicKey;

    @JsonProperty("public_key_all")
    public List<String> publicKeyAll;

    @JsonProperty("resolvers")
    public List<String> resolvers;

    @JsonProperty("service_key")
    public String serviceKey;

    @JsonProperty("service_key_all")
    public List<String> serviceKeyAll;
}
