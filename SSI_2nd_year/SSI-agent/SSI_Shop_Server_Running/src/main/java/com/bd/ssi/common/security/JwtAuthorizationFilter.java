package com.bd.ssi.common.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.bd.ssi.auth.JWTUtil;
import com.bd.ssi.auth.User;
import com.bd.ssi.auth.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * 헤더의 JWT 토큰 유효성 검증 필터
 */
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    private AuthenticationManager authenticationManager;

//    @Autowired
    private UserRepository userRepository;
    private JWTUtil jwtUtil;


    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository, JWTUtil jwtUtil) {
        super(authenticationManager);
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader(JWTUtil.HEADER_STRING);
        if(header == null || !header.startsWith(JWTUtil.TOKEN_PREFIX)){
            chain.doFilter(request, response);
            return;
        }

        Authentication authentication = getUsernamePasswordAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);

//        super.doFilterInternal(request, response, chain);
    }

    /**
     * JWT 토큰을 검증하고 해당 유저정보로 인증 객체를 생성한다.
     * @param request
     * @return
     */
    private Authentication getUsernamePasswordAuthentication(HttpServletRequest request){
        String token = request.getHeader(JWTUtil.HEADER_STRING).replace(jwtUtil.TOKEN_PREFIX, "");
        if(token != null){
            DecodedJWT jwt = jwtUtil.verifyToken(token);
            if(jwt != null){
                String username = jwt.getSubject();
                if(username != null){
                    Optional<User> optional = userRepository.findById(username);
                    if(optional.isPresent()){
                        User user = optional.get();
                        SsiUserDetails userDetails = new SsiUserDetails(user.getUsername(), user.getPassword(), AuthorityUtils.createAuthorityList(user.getRole()));
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, userDetails.getAuthorities());
                        return auth;
                    }
                }
            }
        }
        return null;
    }
}
