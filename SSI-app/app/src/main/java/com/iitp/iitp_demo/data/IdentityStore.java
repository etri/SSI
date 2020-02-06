package com.iitp.iitp_demo.data;

import android.content.Context;

import com.iitp.core.identity.Identity;
import com.iitp.util.FileUtils;

import java.io.IOException;


/**
 * {@link com.coinplug.metadium.core.identity.Identity} 를 저장/로드/삭제 한다.<br/>
 * File "identity" 이름으로 저장된다.
 */
public class IdentityStore{
    private final static String IDENTITY_FILE_NAME = "identity";

    /**
     * Identity 를 저장한다.
     * @param context  android context
     * @param identity 저장한 identity
     * @return 저장 성공 여부
     */
    public static boolean saveIdentity(Context context, Identity identity) {
        try {
            String json = identity.toJson();
            FileUtils.writeFile(context, IDENTITY_FILE_NAME, json.getBytes());
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Identity 를 불러온다.
     * @param context android context
     * @return 저장되어 있는 Identity 가 없는 경우 null 변환
     */
    public static Identity loadIdentity(Context context) {
        byte[] data = FileUtils.readFile(context, IDENTITY_FILE_NAME);
        if (data == null) {
            return null;
        }

        try {
            return Identity.toIdentity(new String(data));
        }
        catch (IOException e) {
            return null;
        }
    }

    /**
     * Identity 를 삭제한다.
     * @param context android context
     */
    public static void removeIdentity(Context context) {
        context.deleteFile(IDENTITY_FILE_NAME);
    }
}
