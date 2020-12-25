package com.bd.ssi.web.controller;

import com.bd.ssi.auth.User;
import com.bd.ssi.auth.UserRepository;
import com.bd.ssi.common.security.SecurityHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * FCM 토큰 처리 클래스
 */
@Service
public class TokenService {

    @Autowired
    UserRepository userRepository;

    public void updateToken(User token){
        User u = userRepository.findById(SecurityHolder.getUsername()).get();
        u.setToken(token.getToken());
        userRepository.save(u);

    }
}
