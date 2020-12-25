package com.bd.ssi.common;

import com.bd.ssi.common.api.ApiResponse;
import com.bd.ssi.common.api.PgMngr;
import com.bd.ssi.common.mapper.MarketDBMappers;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/common")
public class CommonCtrl {
    @Autowired
    Utils utils;

    @Autowired
    MarketDBMappers marketDBMappers;

    /**
     * VC로그인을 위해서 SSI APP에서 받은 VP 검증
     * VP 검증 이후 PG같은 이후 프로세스가 있어 VC로그인 전용으로 분리
     */
    @PostMapping("/verifyingVpForVcLogin")
    @ResponseBody
    public ApiResponse doVerifyingVpForVcLogin(@RequestBody Map<String, String> paramMap){
        String vp = paramMap.get("sendVp");
        String rcvDid = paramMap.get("did");
        String token = paramMap.get("token");

        vp = vp.replaceAll("\"", "");

        String returnVal = "";
        VcVpMngr vcVpMngr = new VcVpMngr();
        boolean verifyOk = vcVpMngr.doVerifyVpOnly(vp);

        //SSI APP에서 받은 VP 검증 Fail.
        if( !verifyOk ){
            returnVal = "fail of verifying";
            return ApiResponse.fail(9999, returnVal);
        }else{
            //SSI APP에서 받은 VP 검증 OK.
            returnVal = "succ of verifying";

            if( !StringUtils.isBlank(rcvDid) && !StringUtils.isBlank(token) ) {
                int count = marketDBMappers.doCheckDidNToken(rcvDid, token);
                if( count < 1 ) {
                    marketDBMappers.doSaveTokenMapping(rcvDid, token);
                }
            }

            return ApiResponse.ok(returnVal);
        }
    }

    /**
     * SSI APP으로부터 받은 VP를 검증. 정상일 경우 PG와 연동.
     * 현재(2020.10.06) pg verify 주소에 SSI APP에서 받은 VP를 전송.
     * 향후 credential에 따라 vc들이 정해지면, 이들 vc를 metadium sdk에 넣어 vp 생성하여 pg verify 주소로 전송.
     */
    @PostMapping("/verifyingVpForPaying")
    @ResponseBody
    public ApiResponse doVerifyingVpForPaying(@RequestBody String vps){
        Map<String, String> rcvVpsMap = new HashMap<>();

        if( !StringUtils.isBlank(vps) ){
            Gson gson = new Gson();
            rcvVpsMap = gson.fromJson(vps, Map.class);
        }

        String returnVal = "";
        VcVpMngr vcVpMngr = new VcVpMngr();
        boolean verifyOk = vcVpMngr.doVerifyVpOnly(vps);
//        boolean verifyOk = true;

        //SSI APP에서 받은 VP 검증 Fail.
        if( !verifyOk ){
            returnVal = "fail of verifying for paying";
            return ApiResponse.fail(9999, returnVal);
        }else{
            //SSI APP에서 받은 VP 검증 OK.
            //PG로 결제 요청
            if( doRequestPg(rcvVpsMap) ){
                returnVal = "succ of verifying for paying & pg check";
            }else{
                returnVal = "failure of verifying for PG";
            }

            return ApiResponse.ok(returnVal);
        }
    }

    /**
     * SSI APP에서 ProductCredential, ProductProofCredential는 VC가 없어 리턴값을 주지 않아
     * 새상품 결제하기 VP를 이용함. 향후 변경 해야할 부분.
     */
    @PostMapping("/verifyingVpForUsedRegi")
    @ResponseBody
    public ApiResponse doVerifyingVpForUsedRegi(@RequestBody String vps){
        Map<String, String> rcvVpsMap = new HashMap<>();

        if( !StringUtils.isBlank(vps) ){
            Gson gson = new Gson();
            rcvVpsMap = gson.fromJson(vps, Map.class);
        }

        String returnVal = "";
        VcVpMngr vcVpMngr = new VcVpMngr();
//        boolean verifyOk = vcVpMngr.doVerifyVpOnly(vp);
        boolean verifyOk = true;

        //SSI APP에서 받은 VP 검증 Fail.
        if( !verifyOk ){
            returnVal = "fail of verifying for Used Registration";
            return ApiResponse.fail(9999, returnVal);
        }else{
            //SSI APP에서 받은 VP 검증 OK.
            //PG로 결제 요청
            if( doRequestPg(rcvVpsMap) ){
                returnVal = "succ of verifying for Used Registration";
            }else{
                returnVal = "failure of Used Registration";
            }

            return ApiResponse.ok(returnVal);
        }
    }

    /**
     * pg로 결제 요청.
     */
    public boolean doRequestPg(Map<String, String> rcvVpsMap){
        String payment_url = "http://129.254.194.112:9006/api/payment";
        String verify_url = "http://129.254.194.112:9006/api/vp/verify";

        PgMngr pgMngr = new PgMngr();
        String rcvMsg = pgMngr.callUrlMngr(payment_url, "");

        if( !StringUtils.isBlank(rcvMsg) && rcvMsg.indexOf("ERROR") == -1 ){
            try {
                String[] split_string = rcvMsg.split("\\.");
//                String base64EncodedHeader = split_string[0];
                String base64EncodedBody = split_string[1];
//                String base64EncodedSignature = split_string[2];

                Base64 base64Url = new Base64(true);
//                String header = new String(base64Url.decode(base64EncodedHeader));
                String body = new String(base64Url.decode(base64EncodedBody));
                Map<String, Object> map = utils.getJsonToMap(body);

                /**
                 * SSI APP으로부터 받은 VP를 검증. 정상일 경우 PG와 연동.
                 * 현재(2020.10.06) pg verify 주소에 SSI APP에서 받은 VP를 전송.
                 * 향후 credential에 따라 vc들이 정해지면, 이들 vc를 metadium sdk에 넣어 vp 생성하여 pg verify 주소로 전송.
                 */
                if( !StringUtils.isBlank(body) && body.indexOf("CardTokenCredential") > 0 ){
                    String pgVerifiedUrl = pgMngr.callUrlMngr( verify_url, pgMngr.getSendVpStr( rcvVpsMap.get("uuid"), (String)map.get("id"), rcvVpsMap.get("vp")) );

                    Map<String, Object> resultMap = utils.getJsonToMap(pgVerifiedUrl);
                    Boolean result = (Boolean)resultMap.get("result");

                    if( !result ){
                        return false;
                    }

                    return true;
                }
            }catch(Exception ex){
                ex.printStackTrace();
                return false;
            }
        }

        return true;
    }

    /**
     * Product, Deal, Product_img 테이블 기본 데이터 6건 제외하고 초기화
     */
    @RequestMapping("/product_init")
    @ResponseBody
    public ApiResponse doSsiShopProductInit(){
        marketDBMappers.doSsiShopProductInit();
        marketDBMappers.doSsiShopDealInit();
        marketDBMappers.doSsiShopProductImgInit();
        return new ApiResponse(ApiResponse.CODE.SUCCESS, "OK", null);
    }

    @RequestMapping("/hello")
    @ResponseBody
    public String doHello(){
        return "Hello";
    }
}
