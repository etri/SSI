package com.bd.ssi.common.auth;

import com.bd.ssi.auth.JWTUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
public class JwtTest {

    @Autowired
    JWTUtil jwtUtil;

    @Test
    public void test() throws InterruptedException {
        String token = jwtUtil.createToken("user");
        System.out.println(token);
        System.out.println(jwtUtil.verifyToken(token));
    }
}
