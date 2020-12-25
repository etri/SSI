package com.iconloop.iitpvault.vo.dto;

import com.iconloop.iitpvault.vo.enumType.InitiateType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class InitiateReq {
    @ApiModelProperty(value = "mail address 또는 phone number")
    String authId;

//    @ApiModelProperty(value = "REGISTER : 분할한 복구 키 저장을 위한 authToken 발급\r\nRECOVERY : 저장한 복구 키 반환")
//    InitiateType type;
}
