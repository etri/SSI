package com.bd.ssi.common.security;

import org.springframework.security.core.context.SecurityContextHolder;


/**
 * Spring Security 홀더에 접근하여 사용자 정보를 조회하는 util 클래스
 */
public class SecurityHolder {
    /**
     * Get user security user.
     *
     * @return the security user
     */
    public static String getUsername(){
        if(SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof String)
            return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return null;
    }
}
