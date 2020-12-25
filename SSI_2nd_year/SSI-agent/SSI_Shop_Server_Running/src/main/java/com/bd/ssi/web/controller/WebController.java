package com.bd.ssi.web.controller;

import com.bd.ssi.auth.User;
import com.bd.ssi.common.api.ApiResponse;
import com.bd.ssi.common.mapper.MarketDBMappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WebController {

    @Autowired
    TokenService tokenService;

    @Autowired
    MarketDBMappers marketDBMappers;

    @RequestMapping("/test")
    @ResponseBody
    public ApiResponse test(){
        return ApiResponse.ok("test");
    }

    /**
     * 사용자 FCM 토큰 업데이트
     * @param token
     * @return
     */
    @RequestMapping("/token")
    @ResponseBody
    public ApiResponse token(@RequestBody User token){
        tokenService.updateToken(token);
        return ApiResponse.ok("ok");
    }
}
