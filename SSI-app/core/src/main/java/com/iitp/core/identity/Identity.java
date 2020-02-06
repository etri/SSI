package com.iitp.core.identity;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 사용자 Identity 정보
 */
public class Identity implements Serializable {
    private static ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
    }

    /** MetaID. IdentityRegistry 의 EIN 을 byte32 hex-string 으로 변환 */
    @JsonProperty("meta_id")
    private String metaId;

    /** 연동된 서비스 리스트 */
    private Map<String, Service> services = new HashMap<>();

    /** 연동된 서비스의 Key 리스트. 백업시만 사용 */
    @JsonProperty("private_keys")
    private List<String> privateKeys = new ArrayList<>();

    /** 사용자 데이터<br/>https://docs.google.com/document/d/1y6OSNlvSgDPFU-RZk4vPKzrUAI0jKIV9NJz9fMPw3nQ 에서 Data category 부분 참조 */
    private Map<String, Object> data = new HashMap<>();

    public Identity() {
    }

    /**
     * construct
     * @param metaId        MetaIdentity contract address
     * @param managementKey owner address, management key
     */
    public Identity(@NonNull String metaId, String managementKey) {
        this.metaId = metaId;
    }

    /**
     * set MetaIdentity contract address
     * @param metaId contract address
     */
    public void setMetaId(String metaId) {
        this.metaId = metaId;
    }

    @JsonSetter("metaId")
    public void setMetaIdBeforeVersion(String metaId) {
        setMetaId(metaId);
    }

    /**
     * Get Meta id. contract address
     * @return contract address
     */
    public String getMetaId() {
        return metaId;
    }

    /**
     * 인증 서비스 추가
     * @param serviceId service id
     * @param service   service 내용
     */
    public void addService(String serviceId, Service service) {
        services.put(serviceId, service);
    }

    /**
     * 해당 service id 에 대한 정보는 얻는다
     * @param serviceId 정보 얻으려는 service id
     * @return 서비스 정보
     */
    public Service getService(String serviceId) {
        return services.get(serviceId);
    }

    /**
     * 모든 연동된 서비스를 반환한다.
     * @return 서비스 정보들
     */
    public Map<String, Service> getServices() {
        Map<String, Service> ret = new HashMap<>();
        ret.putAll(services);
        return ret;
    }

    /**
     * 서비스 연동 시 만든 private key 를 추가한다. 백업시에만 사용
     * @param privateKey private key (hex-string)
     */
    public void addPrivateKey(String privateKey) {
        privateKeys.add(privateKey);
    }

    /**
     * 서비스 연동된 모든 private key 를 반환
     * @return private key list
     */
    public List<String> getPrivateKeys() {
        List<String> ret = new ArrayList<>();
        ret.addAll(privateKeys);
        return ret;
    }

    /**
     * 사용자 정보를 반환
     * @return 사용자 정보
     */
    public Map<String, Object> getData() {
        Map<String, Object> ret = new HashMap<>();
        ret.putAll(data);
        return ret;
    }

    /**
     * to json
     * @return json string
     * @throws JsonProcessingException json error
     */
    @JsonIgnore
    public String toJson() throws JsonProcessingException {
        return mapper.writeValueAsString(this);
    }

    /**
     * json string to identity
     * @param json json string
     * @return identity object
     * @throws IOException read error
     */
    @JsonIgnore
    public static Identity toIdentity(String json) throws IOException {
        return mapper.readValue(json, Identity.class);
    }
}
