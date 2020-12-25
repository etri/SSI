package com.iconloop.iitpvault.vo.dto;

import lombok.Data;

@Data
public class SaveRecoveryReq {
    String authToken;

//    String did;

    String recoveryKey;
}
