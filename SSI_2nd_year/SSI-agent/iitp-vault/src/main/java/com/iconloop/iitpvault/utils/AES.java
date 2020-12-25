package com.iconloop.iitpvault.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;

public class AES {
    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private static final String EncryptionAlgorithm = "AES";
    private static final String EncryptionScheme    = "AES/CBC/PKCS5Padding";
    private static final String Key                 = "ic0n_l00p_d0+c0m_k00k_check_ii+p";
    private static final String InitialVector       = "ic0n_l00p_d0+c0m";

    private static final SecretKeySpec SKS = new SecretKeySpec(Key.getBytes(), EncryptionAlgorithm);
    private static final IvParameterSpec IPS = new IvParameterSpec(InitialVector.getBytes());

    private static final Cipher encCipher = getEncCipher();
    private static final Cipher decCipher = getDecCipher();

    public static Cipher getEncCipher() {
        try {
            Cipher cipher = Cipher.getInstance(EncryptionScheme);
            cipher.init(Cipher.ENCRYPT_MODE, SKS, IPS);
            return cipher;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Cipher getDecCipher() {
        try {
            Cipher cipher = Cipher.getInstance(EncryptionScheme);
            cipher.init(Cipher.DECRYPT_MODE, SKS, IPS);
            return cipher;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encrypt(String data) {
        return new String(Base64.encodeBase64URLSafe(encrypt(data.getBytes(DEFAULT_CHARSET))));
    }

    public static String decrypt(String data) {
        return new String(decrypt(Base64.decodeBase64(data.getBytes(DEFAULT_CHARSET))));
    }

    private static byte[] encrypt(byte[] data) {
        try {
            return encCipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] decrypt(byte[] data) {
        try {
            return decCipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
