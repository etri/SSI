package com.iconloop.iitpvault.exception;


import com.iconloop.iitpvault.exception.error.AuthErrorCode;

public class AuthException extends BaseException {
    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthException(String code, String message) {
        super(code, message);
    }

    public AuthException(String code, String message, long detailCode, String detailMessage) {
        super(code, message, detailCode, detailMessage);
    }

    public AuthException(AuthErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }

    public AuthException(AuthErrorCode errorCode, String option) {
        super(errorCode.getCode(), errorCode.getMessage() + option);
    }
}
