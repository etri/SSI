package com.bd.ssi.common;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class preMakeLoginVcs {
    @Autowired
    VcVpMngr vcVpMngr;

    /**
     * 시연시 초기 데이터 준비과정으로
     *     사전 로그인 VC 생성 후 리턴을 위한 URL. 코인플러그에서 호출 예정.
     */
    @RequestMapping("/pre_make_login_vcs")
    @ResponseBody
    public String getUserInfos(@RequestBody String req){
        String returnVal = "";
        if( !StringUtils.isBlank(req) ) {
            Gson gson = new Gson();
            Map<String, String> rcvMap = new HashMap<String, String>();
            rcvMap = gson.fromJson(req, Map.class);

            if( rcvMap == null ){
                returnVal = "Json을 parsing 할 수 없습니다.";
                return returnVal;
            }

            Map<String, String> user = new HashMap<String, String>();
            user.put("name", rcvMap.get("name"));
            user.put("address", rcvMap.get("address"));
            user.put("birth_date", rcvMap.get("birth_date"));
            user.put("phone_num", rcvMap.get("phone_num"));
            user.put("did", rcvMap.get("did"));

            Map<String, String> signedMap = vcVpMngr.doMakePreVc(user);

            returnVal = String.format("{\"vc\":\"%s\"}", signedMap.get("signedVc"));
        }else{
            returnVal = "필수값이 없습니다.\nReceived Body:" + req;
        }

        return returnVal;
    }
}
