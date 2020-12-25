package com.bd.ssi.common.security;

import com.bd.ssi.auth.*;
import com.bd.ssi.common.api.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * SSI AuthenticationFilter
 * 아이디 패스워드로 인증후 JWT 토큰 발급
 */
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private JWTUtil jwtUtil;

    private UserRepository userRepository;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, UserRepository userRepository, JWTUtil jwtUtil) {
        setAuthenticationManager(authenticationManager);
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 아이디 패스워드로 인증을 수행
     * @param request
     * @param response
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        //get request body
        AuthRequest requestBody = null;
        try {
            requestBody = new ObjectMapper().readValue(request.getInputStream(), AuthRequest.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //create login token
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                requestBody.getUsername(), requestBody.getPassword(), new ArrayList<>());

        return this.getAuthenticationManager().authenticate(authenticationToken);
    }

    /**
     * 인증 통과시 토큰 발급
     * @param request
     * @param response
     * @param chain
     * @param authResult
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        //Grab principal
        UserDetails userDetails = (UserDetails) authResult.getPrincipal();
        String token = JWTUtil.TOKEN_PREFIX + jwtUtil.createToken(userDetails.getUsername());
        response.addHeader(JWTUtil.HEADER_STRING, token);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        User user = userRepository.findById(userDetails.getUsername()).get();
        ObjectMapper om = new ObjectMapper();
        response.getWriter().write(om.writeValueAsString(ApiResponse.ok(new AuthResponse(token, user))));

//        super.successfulAuthentication(request, response, chain, authResult);
    }
}
