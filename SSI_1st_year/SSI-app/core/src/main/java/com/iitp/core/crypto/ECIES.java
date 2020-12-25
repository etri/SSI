package com.iitp.core.crypto;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.util.Arrays;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * ECIES encrypt, decrypt with AES256 <br>
 * Support onnly secp256k1 curve.
 *
 */
public class ECIES {
    private static ECNamedCurveParameterSpec params;
    private static EllipticCurve ellipticCurve;
    static {
        params = ECNamedCurveTable.getParameterSpec("secp256k1");
        ellipticCurve = EC5Util.convertCurve(params.getCurve(), params.getSeed());
    }

    /**
     * encrypt message with encoded ec public-key
     * @param publicKey ec public-key (encoded)
     * @param message   message to encrypt
     * @return cipher text
     * @throws GeneralSecurityException error encrypt
     */
    public static byte[] encrypt(byte[] publicKey, byte[] message) throws GeneralSecurityException {
        return encrypt(toBCECPublicKey(publicKey), message);
    }

    /**
     * encrypt message with ec public-key
     * @param publicKey ec public-key
     * @param message message to encrypt
     * @return cipher text
     * @throws GeneralSecurityException error encrypt
     */
    public static byte[] encrypt(BigInteger publicKey, byte[] message) throws GeneralSecurityException {
        return encrypt(toBCECPublicKey(Arrays.prepend(Numeric.toBytesPadded(publicKey, 64), (byte)0x04)), message);
    }

    /**
     * encrypt message with ec public-key
     * @param recipientPublicKey ec public-key
     * @param message            message to encrypt
     * @return cipher text
     * @throws GeneralSecurityException error encrypt
     */
    public static byte[] encrypt(BCECPublicKey recipientPublicKey, byte[] message) throws GeneralSecurityException {
        // new sender ec key-pair
        KeyPair senderKey = ECKeyUtils.generateSecp256k1KeyPair();
        BCECPrivateKey senderPrivateKey = (BCECPrivateKey)senderKey.getPrivate();
        BCECPublicKey senderPublicKey = (BCECPublicKey)senderKey.getPublic();

        // generate secret key, mac key. key agreement.
        byte[] kEkM = SHA512(generateSecret(senderPrivateKey, recipientPublicKey));
        byte[] aesKey = Arrays.copyOfRange(kEkM, 0, 32);
        byte[] macKey = Arrays.copyOfRange(kEkM, 32, 64);

        // generate iv
        byte[] iv = Arrays.copyOfRange(hMacSHA256(senderPrivateKey.getEncoded(), message), 0, 16);

        // encrypt
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(iv));
        byte[] encryptedData = cipher.doFinal(message);

        // generate mac
        byte[] mac = hMacSHA256(macKey, Arrays.concatenate(iv, encryptedData));

        // encode publicKey + IV + encryptedData + mac
        ByteBuffer buffer = ByteBuffer.allocate(33+16+encryptedData.length+32);
        buffer.put(senderPublicKey.getQ().getEncoded(true));
        buffer.put(iv);
        buffer.put(encryptedData);
        buffer.put(mac);

        return buffer.array();
    }

    /**
     * encrypt with x509 encoded public-key
     * @param x509EncodedPublicKey x509 encoded public-key
     * @param message              to encrypt
     * @return cipher text
     * @throws GeneralSecurityException encrypt error
     */
    public static byte[] encryptWithX509(byte[] x509EncodedPublicKey, byte[] message) throws GeneralSecurityException {
        return encrypt((BCECPublicKey)KeyFactory.getInstance("ECDSA").generatePublic(new X509EncodedKeySpec(x509EncodedPublicKey)), message);
    }

    /**
     * decrypt cipher text with ec private-key
     * @param recipientPrivateKey ec private-key
     * @param cipherText          cipher text to decrypt
     * @return decrypted text
     * @throws GeneralSecurityException error decrypt
     */
    public static byte[] decrypt(BigInteger recipientPrivateKey, byte[] cipherText) throws GeneralSecurityException {
        return decrypt(toBCECPrivateKey(recipientPrivateKey), cipherText);
    }

    /**
     * decrypt cipher text with encoded ec private-key
     * @param recipientPrivateKey encoded ec private-key
     * @param cipherText          cipher text to decrypt
     * @return decrypted text
     * @throws GeneralSecurityException error decrypt
     */
    public static byte[] decrypt(byte[] recipientPrivateKey, byte[] cipherText) throws GeneralSecurityException {
        return decrypt(toBCECPrivateKey(Numeric.toBigInt(recipientPrivateKey)), cipherText);
    }

    /**
     * decrypt cipher text with ec private-key
     * @param recipientPrivateKey ec private-key
     * @param cipherText          cipher text to decrypt
     * @return decrypted text
     * @throws GeneralSecurityException error decrypt
     */
    public static byte[] decrypt(BCECPrivateKey recipientPrivateKey, byte[] cipherText) throws GeneralSecurityException {
        BCECPublicKey senderPublicKey = toBCECPublicKey(Arrays.copyOfRange(cipherText, 0, 33));

        byte[] secret = generateSecret(recipientPrivateKey, senderPublicKey);
        byte[] kEkM = SHA512(secret);
        byte[] aesKey = Arrays.copyOfRange(kEkM, 0, 32);
        byte[] macKey = Arrays.copyOfRange(kEkM, 32, 64);

        byte[] ivct = Arrays.copyOfRange(cipherText, 33, cipherText.length-32);
        byte[] orgMac = Arrays.copyOfRange(cipherText, cipherText.length-32, cipherText.length);
        byte[] mac = hMacSHA256(macKey, ivct);

        if (!Arrays.areEqual(orgMac, mac)) {
            throw new InvalidKeyException("Invalid mac");
        }

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(Arrays.copyOfRange(ivct, 0, 16)));
        return cipher.doFinal(ivct, 16, ivct.length-16);
    }

    /**
     * decrypt with pkcs8 encoded private-key
     * @param pkcs8EncodedPrivate pkcs8 encoded private-key
     * @param message             to encrypt
     * @return cipher text
     * @throws GeneralSecurityException decrypt error
     */
    public static byte[] decryptWithPKCS8(byte[] pkcs8EncodedPrivate, byte[] message) throws GeneralSecurityException {
        return decrypt((BCECPrivateKey)KeyFactory.getInstance("ECDSA").generatePrivate(new PKCS8EncodedKeySpec(pkcs8EncodedPrivate)), message);
    }

    /**
     * encoded ec public-key to BouncyCastle ec public-key object
     * @param publicKey encoded ec public-key
     * @return ec public-key object
     */
    private static BCECPublicKey toBCECPublicKey(byte[] publicKey) {
        ECPoint ecPoint = ECPointUtil.decodePoint(ellipticCurve, publicKey);
        ECParameterSpec params2 = EC5Util.convertSpec(ellipticCurve, params);
        ECPublicKeySpec keySpec = new ECPublicKeySpec(ecPoint, params2);
        return new BCECPublicKey("EC", keySpec, BouncyCastleProvider.CONFIGURATION);
    }

    /**
     * encoded ec private-key to BouncyCastle ec private-key object
     * @param privateKey encoded ec private-key
     * @return private-key object
     */
    private static BCECPrivateKey toBCECPrivateKey(BigInteger privateKey) {
        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(privateKey, EC5Util.convertSpec(ellipticCurve, params));
        return new BCECPrivateKey("EC", privateKeySpec, BouncyCastleProvider.CONFIGURATION);
    }

    /**
     * generate secret with ec private-key and public-key.<br>
     * secret = Q*d
     * @param senderPrivateKey   sender ec private-key
     * @param recipientPublicKey recipient ec public-key
     * @return 32 bytes secret
     */
    private static byte[] generateSecret(BCECPrivateKey senderPrivateKey, BCECPublicKey recipientPublicKey) {
        return Numeric.toBytesPadded(recipientPublicKey.getQ().multiply(senderPrivateKey.getD()).normalize().getAffineXCoord().toBigInteger(), 32);
    }

    /**
     * hMac sha256 utility
     * @param key  hMac key
     * @param data to hash
     * @return hash
     */
    private static byte[] hMacSHA256(byte[] key, byte[] data) {
        HMac hmac = new HMac(new SHA256Digest());
        hmac.init(new KeyParameter(key));
        hmac.update(data, 0, data.length);
        byte[] out = new byte[hmac.getMacSize()];
        hmac.doFinal(out, 0);
        return out;
    }

    /**
     * sha512 utility
     * @param data to hash
     * @return hash
     */
    private static byte[] SHA512(byte[] data) {
        SHA512Digest sha512 = new SHA512Digest();
        byte[] out = new byte[sha512.getDigestSize()];
        sha512.update(data, 0, data.length);
        sha512.doFinal(out, 0);
        return out;
    }
}
