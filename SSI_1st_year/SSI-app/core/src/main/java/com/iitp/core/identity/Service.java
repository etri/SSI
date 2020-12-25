package com.iitp.core.identity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 서비스 연동 정보
 */
public class Service implements Serializable {
    /** address of key */
    public String authKey;
    /** 등록 시간 */
    public long timestamp;
    /** 추가 데이터 */
    public Map<String, String> extra;

    /**
     * 서비스를 위해 생성한 key 를 반환
     * @return address
     */
    @JsonProperty("auth_key")
    public String getAuthKey() {
        return authKey;
    }

    /**
     * 서비스를 위해 생성한 key 를 등록
     * @param authKey address
     */
    @JsonProperty("auth_key")
    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    /**
     * 서비스 연동 시간을 반환
     * @return timestamp millis second
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 서비스 연동 시간을 설정하낟.
     * @param timestamp 등록시간. millis second
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 사용자 추가 데이터를 반환
     * @return 사용자 추가 데이터
     */
    public Map<String, String> getExtra() {
        Map<String, String> ret = new HashMap<>();
        ret.putAll(extra);
        return ret;
    }

    /**
     * 사용자 추가 데이터를 설정한다.
     * @param extra 사용자 추가 데이터
     */
    public void setExtra(Map<String, String> extra) {
        this.extra = extra;
    }
}
