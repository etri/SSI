package com.iconloop.iitpvault.service;

import com.iconloop.iitpvault.exception.AuthException;
import com.iconloop.iitpvault.exception.error.AuthErrorCode;
import com.iconloop.iitpvault.utils.AuthTokenUtil;
import com.iconloop.iitpvault.utils.VerifyTokenUtil;
import com.iconloop.iitpvault.vo.dto.VerifyReq;
import com.iconloop.iitpvault.vo.dto.VerifyRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final AuthTokenUtil authTokenUtil;

    @Value("${auth.code}")
    String code;

    public VerifyRes verify(VerifyReq req) {
        if(VerifyTokenUtil.isValidate(req.getVerifyToken(), req.getAuthCode(), code)) {
            String authId = VerifyTokenUtil.getAuthId(req.getVerifyToken());
            try {
                return VerifyRes.builder().authToken(authTokenUtil.getGenerateToken(authId, new HashMap())).build();
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new AuthException(AuthErrorCode.AUTH_CODE_INVALID);
            }
        } else {
            throw new AuthException(AuthErrorCode.AUTH_CODE_INVALID);
        }
    }
}
