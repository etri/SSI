package com.iconloop.iitpvault.controller;

import com.iconloop.iitpvault.exception.AuthException;
import com.iconloop.iitpvault.service.AuthService;
import com.iconloop.iitpvault.utils.VerifyTokenUtil;
import com.iconloop.iitpvault.vo.ErrorResultVo;
import com.iconloop.iitpvault.vo.ResponseVo;
import com.iconloop.iitpvault.vo.dto.InitiateReq;
import com.iconloop.iitpvault.vo.dto.InitiateRes;
import com.iconloop.iitpvault.vo.dto.VerifyReq;
import com.iconloop.iitpvault.vo.dto.VerifyRes;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("v1/auth")
public class AuthController {

    private final AuthService authService;

    @ApiOperation(value = "인증 email/sms 발송요청", response = InitiateRes.class)
    @PostMapping("/initiate")
    public ResponseEntity initiate(@RequestBody InitiateReq req) {
        String authNum = VerifyTokenUtil.generateRandomKey(6,true);
        String token = VerifyTokenUtil.generateToken(req.getAuthId(), /*req.getType(),*/ authNum);
        log.info("===========================");
        log.info("initiate token :: {}", token);
        return ResponseEntity.ok().body(ResponseVo.builder().success(true).result(InitiateRes.builder().verifyToken(token).build()).build());
    }

    @ApiOperation(value = "email/sms 인증 및 인증 토큰 반환", response = VerifyRes.class)
    @PostMapping("/verify")
    public ResponseEntity verify(@RequestBody VerifyReq req) {
        try {
            log.info("===========================");
            return ResponseEntity.ok().body(ResponseVo.builder().success(true).result(authService.verify(req)).build());
        } catch (AuthException e) {
            log.error("{} : {}", e.getCode(), e.getMessage());
            ErrorResultVo vo = ErrorResultVo.builder().code(e.getCode()).message(e.getMessage()).build();
            return ResponseEntity.ok().body(ResponseVo.builder().success(false).result(vo).build());
        }
    }
}
