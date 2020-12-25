package com.bd.ssi.common.exception;

public class SsiException extends RuntimeException{
    private Integer code;
    public SsiException(Integer code, String message){
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
