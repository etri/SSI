package com.iconloop.iitpvault.vo.dto;

import lombok.Data;

@Data
public class VerifyReq {
    String verifyToken;

    String authCode;
}
