package com.iconloop.iitpvault.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseVo {
    private boolean success;
    private Object result;

    @Builder
    public ResponseVo(boolean success, Object result) {
        this.success = success;
        this.result = result;
    }
}
