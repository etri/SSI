package com.iitp.iitp_demo;

import com.iitp.core.protocol.data.RegistryAddress;

/**
 * Metadium Node, block-explorer, proxy, config, popup notice 등의 URL 과 contract 의 address 정보 설정
 */
public class Constants{

    /**
     * Metadium Node URL
     */
    public static final String META_API_NODE_URL = BuildConfig.MAIN_NET ? "https://api.metadium.com/prod" : "https://api.metadium.com/dev";

    /**
     * proxy server URL
     */
    public final static String PROXY_URL = BuildConfig.MAIN_NET ? "https://delegator.metadium.com" : "https://testdelegator.metadium.com";

    /**
     * Default RegistryAddress
     */
    public static final RegistryAddress DEFAULT_REGISTRY_ADDRESS = new RegistryAddress();

    /**
     * preference key
     */

    public static final String USER_DATA = "userData";
    public static final String HAS_ID = "hasId";
    public static final String META_ID = "metaid";
    public static final String USER_DATA_INDEX = "select_user_index";
    public static final String BLOCKCHAIN_CHECK = "blockchain_index";
    public static final String ALL_DID_DATA = "all_did_data";
    public static final String DID_DATA_INDEX = "select_did_index";
    public static final String CHECK_CREDENTIAL = "check_credential";




    /**
     * secure Preference
     */

    public static final String ID_CREDENTIAL = "id_credential";
//    public static String ID_CREDENTIAL_JSON = "id_credential_json";
    public static final String OFFICE_CREDENTIAL = "office_credential";
//    public static String OFFICE_CREDENTIAL_JSON = "office_credential_json";

    /**
     * intent
     */
    public static final String USER_TYPE = "userType";
    public static final String USER_DATA_DETAIL = "userDataDetail";
    public static final String RESPONSE_DATA = "responseData";

    public static final String TAB_INDEX = "tab_index";




}
