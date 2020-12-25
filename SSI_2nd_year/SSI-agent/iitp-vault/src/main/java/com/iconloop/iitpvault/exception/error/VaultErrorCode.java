package com.iconloop.iitpvault.exception.error;

import lombok.Getter;

@Getter
public enum VaultErrorCode {
    UNKNOWN_RESTORE_TYPE( "VA0001","Unknown restore type."),
    NOT_EXIST_RECOVERY_DATA("VA0002", "Not exist recovery data."),
    ;


    private String code;
    private String message;

    VaultErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}