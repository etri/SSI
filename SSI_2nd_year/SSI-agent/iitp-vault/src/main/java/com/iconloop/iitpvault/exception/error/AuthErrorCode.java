package com.iconloop.iitpvault.exception.error;

import lombok.Getter;

@Getter
public enum AuthErrorCode {
    AUTH_CODE_INVALID( "AU0001","AuthCode invalid."),
    AUTH_TOKEN_EXPIRED("AU0002", "AuthToken invalid."),
    ;


    private String code;
    private String message;

    AuthErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
