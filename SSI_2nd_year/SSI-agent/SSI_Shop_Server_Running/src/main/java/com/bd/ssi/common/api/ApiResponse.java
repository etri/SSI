package com.bd.ssi.common.api;

/**
 * API 응답 클래스
 */
public class ApiResponse {
    public static class CODE {
        public static final int SUCCESS = 1000;
        public static final int FAIL_SERVER_ERROR = 9999;
    }

    private Integer code;
    private Object data;
    private String message;

    public ApiResponse(Integer code, Object data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public static ApiResponse ok(Object data) {
        return new ApiResponse(CODE.SUCCESS, data, null);
    }
    public static ApiResponse fail(Integer code, String message){
        return new ApiResponse(code, null, message);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
