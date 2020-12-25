package com.iconloop.iitpvault.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@Getter
@Setter
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
class BaseException extends RuntimeException{

    private String code;
    private String message;
    private long detailCode;
    private String detailMessage;

    BaseException(String message) {
        super(message);
        this.message = message;
    }

    BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    BaseException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    BaseException(String code, String message, long detailCode, String detailMessage) {
        super(message);
        this.code = code;
        this.message = message;
        this.detailCode = detailCode;
        this.detailMessage = detailMessage;
    }
}
