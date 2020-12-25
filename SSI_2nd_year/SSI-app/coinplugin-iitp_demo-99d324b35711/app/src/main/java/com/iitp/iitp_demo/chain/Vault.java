package com.iitp.iitp_demo.chain;

import android.content.Context;

import com.iitp.iitp_demo.util.PrintLog;
import com.nomadconnection.vault.RecoveryKey;
import com.nomadconnection.vault.SharingCipher;

import java.security.GeneralSecurityException;
import java.util.List;

public class Vault{


    private static Vault instance;

    private Context ctx;

    private Vault(){

    }

    public static synchronized Vault getInstance(){
        if(instance == null){
            instance = new Vault();
        }
        return instance;
    }

    public String createRecoveryKey(){
        RecoveryKey recoveryKey = RecoveryKey.create();
        byte[] v = recoveryKey.getV(); // get bytes of `v`
        String hexV = recoveryKey.asHex(); // get hex string of `v`
        return hexV;
    }

    public List<String> spiltRecoveryKey(RecoveryKey recoveryKey, int n){
        List<String> vi = recoveryKey.split(n); // hex string
        return vi;
    }

    public RecoveryKey mergeRecoveryKey(List<String> spiltKey){
        RecoveryKey reconstructKey = RecoveryKey.reconstruct(spiltKey);
        return reconstructKey;
    }

    public RecoveryKey getRecoveryKey(String v){
//        String v = "73cc05e60f68d90dc0e1820962d2e8a4";
        RecoveryKey recoveryKey = RecoveryKey.valueOf(v);
        return recoveryKey;
    }

    public RecoveryKey getRecoveryKey(byte[] v){
        RecoveryKey recoveryKey = RecoveryKey.valueOf(v);
        return recoveryKey;
    }

    public String getRecoveryKey(RecoveryKey v){
        String hexV = v.asHex(); // get hex string of `v`
        return hexV;
    }

    public List<String> encryptData(RecoveryKey recoveryKey, int threshold, int n, String message){
        SharingCipher cipher = new SharingCipher(recoveryKey, n, threshold);
//        String secret = "hello"; // secret data
// Encrypt secret data with n shared clues
        List<String> sharedClues = null; // hex string
        try{
            sharedClues = cipher.encrypt(message);
        }catch(GeneralSecurityException e){
            PrintLog.e("encryptData error");
//            e.printStackTrace();
        }
        return sharedClues;
    }

    public String decryptData(String v, List<String> sharedClues, int threshold, int n){// n= 2, threshold = 2
        RecoveryKey recoveryKey = RecoveryKey.valueOf(v);
        SharingCipher cipher = new SharingCipher(recoveryKey, n, threshold); // Encrypted shared clues
        // Decrypt n shared clues
        SharingCipher.Bytes secret = null;
        String data = null;
        try{
            secret = cipher.decrypt(sharedClues);
            // secret.asBytesArray(); // Decrypted secret data
            data = secret.asString(); // Decrypted secret d ata string (UTF_8) secret.asHex(); // Decrypted secret data hex string
        }catch(GeneralSecurityException e){
//            e.printStackTrace();
            PrintLog.e("encryptData error");
        }
        return data;
    }

}
