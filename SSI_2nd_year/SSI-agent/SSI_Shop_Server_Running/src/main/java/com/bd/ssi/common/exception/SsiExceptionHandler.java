package com.bd.ssi.common.exception;

import com.bd.ssi.common.api.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 예외 처리 컨트롤러 어드바이스
 */
@ControllerAdvice
public class SsiExceptionHandler {

    @Autowired
    Environment env;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @ExceptionHandler(SsiException.class)
    @ResponseBody
    public ApiResponse handleSsiException(SsiException e){
        logger.error("SSI API error", e);
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ApiResponse handleException(Exception e) {
        logger.error("server error", e);
        return ApiResponse.fail(ApiResponse.CODE.FAIL_SERVER_ERROR, env.getProperty(String.valueOf(ApiResponse.CODE.FAIL_SERVER_ERROR)));
    }
}
