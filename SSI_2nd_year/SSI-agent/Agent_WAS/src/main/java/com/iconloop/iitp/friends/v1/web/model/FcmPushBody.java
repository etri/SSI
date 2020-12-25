package com.iconloop.iitp.friends.v1.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Firebase Push의 body에 들어갈 json 내용.
 */
@Builder
@AllArgsConstructor
@Getter
public class FcmPushBody {
    private String action;
    private String token;
}
