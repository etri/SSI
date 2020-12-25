package com.iconloop.iitpvault.utils;

import com.iconloop.iitpvault.exception.AuthException;
import com.iconloop.iitpvault.exception.error.AuthErrorCode;
import foundation.icon.did.core.AlgorithmProvider;
import foundation.icon.did.document.EncodeType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class AuthTokenUtil {

    private static final String secret = EncodeType.HEX.encode(AlgorithmProvider.secureRandom().generateSeed(16));

//    private String secret = ;

    private final long expire = 5 * 60 * 1000L; // 5분

    public String getAuthIdFromRequest(HttpServletRequest request) {
        try {
            final String requestTokenHeader = request.getHeader("Authorization");
            if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
                String jwtToken = requestTokenHeader.substring(7);
                return getAuthId(jwtToken);
            } else {
                return null;
            }
        } catch (SignatureException exception) {
            log.error("token signature error : {}", exception.getMessage());
            return null;
        }
    }

    public String getAuthId(String token) {
        if(isTokenExpired(token)) {
            throw new AuthException(AuthErrorCode.AUTH_TOKEN_EXPIRED);
        }
        return getUsernameFromToken(token);
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // private
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    public String getGenerateToken(String authId, Map claim) {
        return Jwts.builder()
                .setClaims(claim)
                .setSubject(authId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    public String getGenerateToken(String authId, Map claim, long expire) {
        return Jwts.builder()
                .setClaims(claim)
                .setSubject(authId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }
}
