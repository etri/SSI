package com.iconloop.iitpvault.vo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
public class InitiateRes {
    @ApiModelProperty(value = "인증 번호화 함께 제출할 인증용 임시토큰")
    String verifyToken;

    @Builder
    public InitiateRes(String verifyToken) {
        this.verifyToken = verifyToken;
    }
}
