package com.iconloop.iitp.friends.v1.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * REST API 응답 포맷.
 */
@Builder
@AllArgsConstructor
@Getter
public class ResponseData {
    private boolean status;
    private String errorMessage;

    private List<Friend> friends;
    // 위임장 전송 요청에 대한 응답 토큰.
    private String token;

    private String holderDid;
    private String holderVc;
    private String poaVc;

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Friend {
        private String did;
        private String name;
    }
}
