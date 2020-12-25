package com.iitp.iitp_demo.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iitp.core.protocol.data.RegistryAddress;

/**
 * Keepin 기반 정보를 관리
 */
public class RegistryManager{
    /** preference name to save cache data */
    private final static String PREF_META_REGISTRY = "meta_registry";

    /** registry address info */
    private final static String KEY_CONTRACT_ADDRESS = "contract_address";

    /** service info */
    private final static String KEY_SERVICE_INFO = "service_info";

    private static ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
    }

    private static RegistryAddress registryAddress;

    /**
     * get preference
     */
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_META_REGISTRY, Context.MODE_PRIVATE);
    }

    /**
     * Proxy 에서 내려주는 Service address 들의 정보({@link com.coinplug.metadium.core.protocol.MetaProxy#getAllServiceAddress()})를 저장한다.
     * @param context         android context
     * @param registryAddress address 들의 정보
     * @throws JsonProcessingException parsing error
     */
    public static void setRegistryAddress(Context context, RegistryAddress registryAddress) throws JsonProcessingException{
        RegistryManager.registryAddress = registryAddress;
        getSharedPreferences(context).edit().putString(KEY_CONTRACT_ADDRESS, objectMapper.writeValueAsString(registryAddress)).apply();
    }

    /**
     * roxy 에서 내려주는 Service address 들의 정보({@link com.coinplug.metadium.core.protocol.MetaProxy#getAllServiceAddress()})를 얻는다.
     * @param context android context
     * @return address 들의 정보
     */
    public static RegistryAddress getRegistryAddress(Context context) {
        if (registryAddress == null) {
            try {
                registryAddress = objectMapper.readValue(getSharedPreferences(context).getString(KEY_CONTRACT_ADDRESS, null), RegistryAddress.class);
            }
            catch (Exception e) {
                return null;
            }
        }
        return registryAddress;
    }

}
