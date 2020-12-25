package com.iitp.iitp_demo.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.util.Base64;

import androidx.biometric.BiometricManager;

import com.iitp.iitp_demo.activity.SplashActivity;

import java.security.KeyStore;

import static android.content.Context.MODE_PRIVATE;

/**
 * Biometric util
 */
public class BiometricUtils{

    public static final String SET_FINGERPRINT = "set_fingerprint";
    public static final String PINCODE = "pincode";

    /**
     * check fingerprint
     *
     * @param context android context
     * @return has hardware and enrolled fingerprint
     */
    public static boolean checkFingerprint(Context context){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
            if(fingerprintManager != null && fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints()){
                return true;
            }
        }
        return false;
    }

    /**
     * {@link SecureSharedPreferences} 를 생성한다.<br/>
     * 모든 값은 AndroidKeystore 통해 안전하게 보관
     *
     * @param context android context
     * @return SecureSharedPreferences
     */
    private static SharedPreferences getPreference(Context context){
        return CommonPreference.getInstance(context).getSecureSharedPreferences();
    }

    public static void enableFingerPrint(Context context){
        getPreference(context).edit().putBoolean(SET_FINGERPRINT, true).apply();
    }

    public static boolean isFingerPrint(Context context){
        return getPreference(context).getBoolean(SET_FINGERPRINT, false);
    }

    public static void disableFingerPrint(Context context){
        getPreference(context).edit().remove(SET_FINGERPRINT).apply();
    }

    public static void setPincode(Context context, String pincode){
        getPreference(context).edit().putString(PINCODE, pincode).apply();
    }

    public static String getPincode(Context context){
        return getPreference(context).getString(PINCODE, null);
    }

    public static void removePincode(Context context){
        getPreference(context).edit().remove(PINCODE).apply();
    }

    public static boolean hasBiometricEnrolled(Context context){
        boolean rtn = false;
        BiometricManager biometricManager =  BiometricManager.from(context);
        int canAuthenticate = biometricManager.canAuthenticate();
        PrintLog.e("canAuthenticate = "+canAuthenticate);
        if(canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS){
            rtn = true;
        }
        return  rtn;
    }
}
