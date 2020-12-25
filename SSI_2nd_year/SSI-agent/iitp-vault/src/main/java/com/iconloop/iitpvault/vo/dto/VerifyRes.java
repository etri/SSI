package com.iconloop.iitpvault.vo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
public class VerifyRes {
    @ApiModelProperty(value = "백업/복구 시 제출해야 할 인증토큰")
    String authToken;

    @Builder
    public VerifyRes(String authToken) {
        this.authToken = authToken;
    }
}
