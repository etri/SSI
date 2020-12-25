package com.iitp.core.crypto;

import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyPairGeneratorSpi;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.web3j.crypto.ECKeyPair;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;

/**
 * EC keypair 생성 utility<br>
 * 4.4 이하 버전에서 SecurityProvider 를 외부 BouncyCastle 로 변경하면 AndroidKeyStore 에서 Certificate 를 생성하지 못함.<br>
 * 외부 BouncyCastle 로 변경하지 않으면 안드로이드에는 KeyPairGenerator 로 EC algorithm 이 없어서 생성 할 수 없음.<br>
 * 직접 BouncyCastle 의 KeyPairGenerator 를 직접 호출하여 keypair 를 생성함<br>
 */
public class ECKeyUtils {
    public static ECKeyPair generateSecp256k1ECKeyPair() throws InvalidAlgorithmParameterException {
        return ECKeyPair.create(generateSecp256k1KeyPair());
    }

    public static KeyPair generateSecp256k1KeyPair() throws InvalidAlgorithmParameterException {
        KeyPairGeneratorSpi.EC ec = new KeyPairGeneratorSpi.EC();
        ec.initialize(new ECGenParameterSpec("secp256k1"), new SecureRandom());
        return ec.generateKeyPair();
    }

    /**
     * Convert from BigInteger to ECPrivateKey
     * @param privateKey private key
     * @param curveName EC curve name
     * @return EC private key
     */
    public static BCECPrivateKey toECPrivateKey(BigInteger privateKey, String curveName) {
        ECNamedCurveParameterSpec params = ECNamedCurveTable.getParameterSpec(curveName);
        EllipticCurve ellipticCurve = EC5Util.convertCurve(params.getCurve(), params.getSeed());

        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(privateKey, EC5Util.convertSpec(ellipticCurve, params));
        return new BCECPrivateKey("EC", privateKeySpec, BouncyCastleProvider.CONFIGURATION);
    }

    /**
     * Convert from encoded public key to ECPublicKey
     * @param encodedPublicKey encoded public key. (compressed or uncompressed)
     * @param curveName EC curve name
     * @return EC public key
     */
    public static BCECPublicKey toECPublicKey(byte[] encodedPublicKey, String curveName) {
        ECNamedCurveParameterSpec params = ECNamedCurveTable.getParameterSpec(curveName);
        EllipticCurve ellipticCurve = EC5Util.convertCurve(params.getCurve(), params.getSeed());

        ECPoint ecPoint = ECPointUtil.decodePoint(ellipticCurve, encodedPublicKey);
        ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(ecPoint, EC5Util.convertSpec(ellipticCurve, params));
        return new BCECPublicKey("EC", publicKeySpec, BouncyCastleProvider.CONFIGURATION);
    }

}
