package com.iconloop.iitpvault.vo.dto;

import lombok.Data;

@Data
public class SaveClueReq {
    String authToken;
    String recoveryClue;
    String dataClue;
}
