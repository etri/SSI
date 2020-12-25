package com.iitp.iitp_demo;

/**
 * Metadium Node, block-explorer, proxy, config, popup notice 등의 URL 과 contract 의 address 정보 설정
 */
public class Constants{


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
    public static final String DID_LIST = "did_list";
    public static final String DELEGATOR_TOKEN = "token";
    public static final String DELEGATOR_TOKEN_SET = "token_set";

    /**
     * intent
     */
    public static final String USER_TYPE = "userType";
    public static final String USER_DATA_DETAIL = "userDataDetail";
    public static final String RESPONSE_DATA = "responseData";

    public static final String TAB_INDEX = "tab_index";


    public static final String DID_NICK = "did_nick";
    public static final String DID_INDEX = "did_index";
    public static final String DID_NAME = "did_name";

    public static final String DID = "did";
    public static final String DID_DATA = "did_data";
    public static final String JWT_DATA = "jwt_data";

    //url
    public static final String gover_url = "http://129.254.194.112:9001/api/create/vc/id";  //행안부
    public static final String gover_url_zkp = "http://129.254.194.112:9001/api/create/zkpvc/id";  //행안부
    public static final String response_url = "http://129.254.194.112:9001/api/vp/verify"; //증권사
    public static final String tele_url = "http://129.254.194.112:9001/api/create/vc/mobile";//통신사
    public static final String card_url = "http://129.254.194.112:9007/api/create/vc";//카드사
    public static final String post_url = "http://129.254.194.112:9008/api/create/vc";//우체국
    public static final String stock_url = "http://129.254.194.112:9005/api/create/vc"; //증권사
    public static final String login_url = "http://129.254.194.112:8080/pre_make_login_vcs"; //로그인
    public static final String school_add_url = "http://129.254.194.112:9003/api/student/add"; //대학 학생추가
    public static final String universe_vprequest_url = "http:// 129.254.194.112:9003/api/vp/verify";//대학
    public static final String universe_company_url = "http:// 129.254.194.112:9002/api/vp/verify";//채용사
    public static final String universe_verify_url = "http:// 129.254.194.112:9004/api/vp/verify";//인증서버
    public static final String universe_stock_url = "http:// 129.254.194.112:9005/api/vp/verify"; //증권사

    public static final String gover_url_zkp_get_offer = "http://129.254.194.112:9001/api/create/cred/id";  //행안부

}
