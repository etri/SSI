package com.bd.ssi.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Jwt 토큰 유틸 클래스
 */
@Component
public class JWTUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public final String ISSUER = "SSI_SHOP";
    public final Long EXPIRE_TIME = 1000L * 60 * 60 * 24 * 7; //7일
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    private String SECRET = "SSI_SHOP_SECRET_12415";
    private Algorithm algorithm;

    private String REFRESH_SECRET = "SSI_SHOP_REFRESH_SECRET_12415";
    private Algorithm refreshAlgorithm;

    private JWTVerifier verifier;

    public JWTUtil(){
        algorithm = Algorithm.HMAC256(SECRET);
        refreshAlgorithm = Algorithm.HMAC256(REFRESH_SECRET);
        verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
//                .withSubject(SUBJECT)
                .build();
    }

    /**
     * 해당 username에 대해 JWT 토큰 생성
     * @param username
     * @return
     */
    public String createToken(String username){
        Date currentDate = new Date();
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(username)
                .withIssuedAt(currentDate)
                .withExpiresAt(new Date(currentDate.getTime() + EXPIRE_TIME))
                .withClaim("username", username)
                .sign(algorithm);
    }

    /**
     * 토큰을 검증
     * @param token
     * @return
     */
    public DecodedJWT verifyToken(String token){
        try {
            DecodedJWT jwt = verifier.verify(token);
            return jwt;
        } catch (Exception exception){
            logger.error("verifyFail", exception);
            return null;
        }

    }

}
