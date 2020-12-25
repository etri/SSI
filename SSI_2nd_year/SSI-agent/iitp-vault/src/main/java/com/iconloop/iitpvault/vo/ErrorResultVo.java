package com.iconloop.iitpvault.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResultVo {
    private String code;
    private String message;

    @Builder
    public ErrorResultVo(String code, String message) {
        this.code = code;
        this.message = message;
    }
}

