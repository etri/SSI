package com.iconloop.iitpvault.utils;

import com.iconloop.iitpvault.vo.enumType.InitiateType;
import org.springframework.util.StringUtils;

import java.util.Random;

public class VerifyTokenUtil {

    private static final long exp = 1000 * 60 * 5;
    private static Random random = new Random();

    public static String generateRandomKey(int length, boolean isDuplicate) {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<length; i++) {
            String s = String.valueOf(random.nextInt(10));
            if(isDuplicate) {
                sb.append(s);
            } else {
                if(sb.indexOf(s) < 0) {
                    sb.append(s);
                } else {
                    i -= 1;
                }
            }
        }
        return sb.toString();
    }

    public static String generateToken(String authId, /*InitiateType type, */String authCode) {
        //문자열에 공백이 아닌 문자가 들어가 있는지 검사
        if(!StringUtils.hasText(authId)) {
            return "";
        }

        long expiration = System.currentTimeMillis() + exp; //5minutes

        return AES.encrypt(authId + "|" + expiration /*+ "|" + type.name()*/  + "|" + authCode);
    }

    public static String getAuthId(String token) {
        return getTokenParam(token, 0);
    }

    public static boolean isValidate(String token) {
        long expiration = Long.valueOf(getTokenParam(token, 1));

        if (System.currentTimeMillis() < expiration) {
            return true;
        }
        return false;
    }

    public static boolean isValidate(String token, String authCode, String pass) {
        long expiration = Long.valueOf(getTokenParam(token, 1));

        if (System.currentTimeMillis() > expiration) {
            return false;
        }
        String tokenAuthCode = getAuthCode(token);
        if(authCode.equals(pass)) {
            return true;
        }
        return tokenAuthCode.equals(authCode)?true:false;
    }
    public static String getAuthCode(String token) {
//        return getTokenParam(token, 3);
        return getTokenParam(token, 2);
    }
//    public static InitiateType getType(String token) {
//        String name = getTokenParam(token, 2);
//        return InitiateType.valueOf(name);
//    }

    public static String getTokenParam(String token, int index) {
        return AES.decrypt(token).split("\\|")[index];
    }
}
