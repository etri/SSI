package com.iitp.util.secure;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;


import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

/**
 * AndroidKeyStore utility<br>
 *
 */
public class KeyStoreUtils {
    /** Android KeyStore provider name */
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";

    /** RSA encryption algorithm */
    private static final String CIPHER_TRANSFORM = "RSA/ECB/PKCS1Padding";

    /**
     * get keystore entry
     * @param alias entry alias
     * @return keystore entry. if not exits, null
     */
    public static KeyStore.Entry getKeyStoreEntry(String alias) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, IOException, CertificateException{
        KeyStore ks = KeyStore.getInstance(ANDROID_KEYSTORE);
        ks.load(null);
        return ks.getEntry(alias, null);
    }

    /**
     * delete keystore entry
     * @param alias entry alias
     * @throws Exception cipher exception
     */
    public static void deleteKeyStoreEntry(String alias) throws Exception{
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        keyStore.deleteEntry(alias);
    }

    /**
     * create keystore with RSA/ECB/PKCS1Padding non-user-authentication
     * @param context context
     * @param alias entry alias
     * @return keystore entry
     * @throws Exception cipher exception
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static KeyStore.Entry createKeyStoreEntryRsaEcbPkcs1Padding(Context context, String alias) throws Exception{
        return createKeyStoreEntryRsaEcbPkcs1Padding(context, alias, false);
    }

    /**
     * create keystore with RSA/ECB/PKCS1Padding
     * @param context context
     * @param alias entry alias
     * @param userAuthenticationRequired use user authentication required
     * @return keystore entry
     * @throws Exception cipher exception
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static KeyStore.Entry createKeyStoreEntryRsaEcbPkcs1Padding(Context context, String alias, boolean userAuthenticationRequired) throws Exception{
        KeyStore.Entry entry = getKeyStoreEntry(alias);

        if (entry == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE);
                kpg.initialize(new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                        .setUserAuthenticationRequired(userAuthenticationRequired)
                        .build());
                kpg.generateKeyPair();
            }
            else {
                Locale initialLocale = Locale.getDefault();
                setLocale(context, Locale.ENGLISH);

                try {
                    final Calendar start = new GregorianCalendar();
                    final Calendar end = new GregorianCalendar();
                    end.add(Calendar.YEAR, 1000);

                    KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE);
                    kpg.initialize(new KeyPairGeneratorSpec.Builder(context)
                            .setAlias(alias)
                            .setSubject(new X500Principal("CN=" + alias))
                            .setSerialNumber(BigInteger.ONE)
                            .setStartDate(start.getTime())
                            .setEndDate(end.getTime())
                            .build());
                    kpg.generateKeyPair();
                }
                finally {
                    setLocale(context, initialLocale);
                }
            }
        }

        return getKeyStoreEntry(alias);
    }

    /**
     * encrypt data with RSA/ECB/PKCS1Padding for KeyStore
     * @param entry keystore entry
     * @param src data to encrypt
     * @return encrypted data
     * @throws Exception cipher exception
     */
    public static byte[] encryptRsaEcbPkcs1Padding(KeyStore.Entry entry, byte[] src) throws Exception{
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORM);

        PublicKey publicKey = ((KeyStore.PrivateKeyEntry)entry).getCertificate().getPublicKey();
        publicKey = KeyFactory.getInstance(publicKey.getAlgorithm()).generatePublic(new X509EncodedKeySpec(publicKey.getEncoded())); // 6.0 bug

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(src);
    }

    /**
     * encrypt data with RSA/ECB/PKCS1Padding for KeyStore
     * @param alias keystore entry alias
     * @param src data to encrypt
     * @return encrypted data
     * @throws Exception cipher exception
     */
    public static byte[] encryptRsaEcbPkcs1Padding(String alias, byte[] src) throws Exception{
        return encryptRsaEcbPkcs1Padding(getKeyStoreEntry(alias), src);
    }

    /**
     * decrypt data with RSA/ECB/PKCS1Padding for KeyStore
     * @param entry keystore entry
     * @param src   data to decrypt
     * @return decrypted data
     * @throws Exception cipher exception
     */
    public static byte[] decryptRsaEcbPkcs1Padding(KeyStore.Entry entry, byte[] src) throws Exception{
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORM);
        cipher.init(Cipher.DECRYPT_MODE, ((KeyStore.PrivateKeyEntry) entry).getPrivateKey());
        return cipher.doFinal(src);
    }

    /**
     * decrypt data with RSA/ECB/PKCS1Padding for KeyStore
     * @param alias keystore entry alias
     * @param src   data to decrypt
     * @return decrypted data
     * @throws Exception cipher exception
     */
    public static byte[] decryptRsaEcbPkcs1Padding(String alias, byte[] src) throws Exception{
        return decryptRsaEcbPkcs1Padding(getKeyStoreEntry(alias), src);
    }

    /**
     * encrypt aes with rsa<br>
     * 256bit encrypted secret key + cipher text
     * @param entry keystore entry
     * @param src   data to encrypt
     * @return encrypted data
     * @throws Exception cipher exception
     */
    public static byte[] encryptAesWithRsa(KeyStore.Entry entry, byte[] src) throws Exception{
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256, new SecureRandom());
        SecretKey secretKey = keyGenerator.generateKey();

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] cipherText = cipher.doFinal(src);

        byte[] cipherSecret = encryptRsaEcbPkcs1Padding(entry, secretKey.getEncoded());

        ByteBuffer buffer = ByteBuffer.allocate(cipherText.length+cipherSecret.length);
        buffer.put(cipherSecret);
        buffer.put(cipherText);
        return buffer.array();
    }

    /**
     * encrypt aes with rsa<br>
     * show {@link #encryptAesWithRsa(KeyStore.Entry, byte[])}
     * @param alias alias of keystore entry
     * @param src   data to encrypt
     * @return encrypted data
     * @throws Exception cipher exception
     */
    public static byte[] encryptAesWithRsa(String alias, byte[] src) throws Exception{
        return encryptAesWithRsa(getKeyStoreEntry(alias), src);
    }

    /**
     * decrypt aes with rsa
     * @param entry keystore entry
     * @param src   data to decrypt
     * @return decrypted data
     * @throws Exception cipher exception
     */
    public static byte[] decryptAesWithRSA(KeyStore.Entry entry, byte[] src) throws Exception{
        ByteBuffer buffer = ByteBuffer.wrap(src);
        byte[] cipherSecret = new byte[256];
        buffer.get(cipherSecret);

        byte[] secret = decryptRsaEcbPkcs1Padding(entry, cipherSecret);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secret, "AES"));
        return cipher.doFinal(src, 256, src.length-256);
    }

    /**
     * decrypt aes with rsa
     * @param alias alias of keystore entry
     * @param src   data to decrypt
     * @return decrypted data
     * @throws Exception cipher exception
     */
    public static byte[] decryptAesWithRSA(String alias, byte[] src) throws Exception{
        return decryptAesWithRSA(getKeyStoreEntry(alias), src);
    }

    /**
     * get Cipher to encrypt. algorithm is RSA/ECB/PKCS1Padding
     * @param alias entry alias
     * @return initialized cipher
     * @throws Exception cipher exception
     */
    public static Cipher getEncryptionCipher(String alias) throws Exception{
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        keyStore.getKey(alias, null);

        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORM);
        PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();
        PublicKey unrestricted = KeyFactory.getInstance(publicKey.getAlgorithm())
                .generatePublic(new X509EncodedKeySpec(publicKey.getEncoded()));

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher;
    }

    /**
     * get cipher to decrypt. algorithm is RSA/ECB/PKCS1Padding
     * @param alias alias of keystore entry
     * @return initialized cipher
     * @throws Exception cipher exception
     */
    public static Cipher getDecryptionCipher(String alias) throws Exception{
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORM);
        cipher.init(Cipher.DECRYPT_MODE, ((KeyStore.PrivateKeyEntry)getKeyStoreEntry(alias)).getPrivateKey());
        return cipher;
    }

    /**
     * get crypto object. algorithm is RSA/ECB/PKCS1Padding
     * @param alias entry alias
     * @param opmode operation mode of Cipher. {@link Cipher#ENCRYPT_MODE}, {@link Cipher#DECRYPT_MODE}
     * @return cipher exception
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static FingerprintManager.CryptoObject getCryptoObjectCipher(String alias, int opmode) {
        try {
            if (opmode == Cipher.ENCRYPT_MODE) {
                return new FingerprintManager.CryptoObject(getEncryptionCipher(alias));
            } else {
                return new FingerprintManager.CryptoObject(getDecryptionCipher(alias));
            }

        }
        catch (Exception e) {
            try {
                deleteKeyStoreEntry(alias);
            }
            catch (Exception e2) {
                Log.e("FingerprintManager", "exception error");
                // no error
            }
        }
        return null;
    }

    /**
     * create keystore entry. SHA256WithRSA Signature. default non-user-authentication
     * @param context current context
     * @param alias alias of keystore entry
     * @return keystore entry
     * @throws Exception cipher exception
     */
    public static KeyStore.Entry createKeyStoreEntrySHA256WithRSA(Context context, String alias) throws Exception{
        return createKeyStoreEntrySHA256WithRSA(context, alias, false);
    }

    /**
     * create keystore entry. SHA256WithRSA Signature.
     * @param context                current context
     * @param alias                  alias of keystore entry
     * @param authenticationRequired use user authentication
     * @return keystore entry
     * @throws Exception cipher exception
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static KeyStore.Entry createKeyStoreEntrySHA256WithRSA(Context context, String alias, boolean authenticationRequired) throws Exception{
        KeyStore.Entry entry = getKeyStoreEntry(alias);

        if (entry == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
                kpg.initialize(new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                        .setDigests(KeyProperties.DIGEST_SHA256)
                        .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                        .setUserAuthenticationRequired(authenticationRequired)
                        .build());
                kpg.generateKeyPair();
            }
            else {
                Locale initialLocale = Locale.getDefault();
                setLocale(context, Locale.ENGLISH);

                final Calendar start = new GregorianCalendar();
                final Calendar end = new GregorianCalendar();
                end.add(Calendar.YEAR, 1000);

                try {
                    KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
                    kpg.initialize(new KeyPairGeneratorSpec.Builder(context)
                            .setAlias(alias)
                            .setSubject(new X500Principal("CN=" + alias))
                            .setSerialNumber(BigInteger.ONE)
                            .setStartDate(start.getTime())
                            .setEndDate(end.getTime())
                            .build());
                    kpg.generateKeyPair();
                }
                finally {
                    setLocale(context, initialLocale);
                }
            }
        }

        return getKeyStoreEntry(alias);
    }

    /**
     * get crypto object SAH256WithRSA signature
     * @param alias alias of keystore entry
     * @return crypto object
     * @throws Exception cipher exception
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static FingerprintManager.CryptoObject getCryptoObjectSHA256WithRSA(String alias) throws Exception{
        Signature sig = Signature.getInstance("SHA256WithRSA");
        sig.initSign(((KeyStore.PrivateKeyEntry)getKeyStoreEntry(alias)).getPrivateKey());
        return new FingerprintManager.CryptoObject(sig);
    }

    /**
     * signing data with SHA256WithRSA
     * @param alias   alias of keystore entry
     * @param message data to sign
     * @return signed message
     * @throws Exception cipher exception
     */
    public static byte[] signatureSHA256WithRSA(String alias, byte[] message) throws Exception{
        Signature sig = Signature.getInstance("SHA256WithRSA");
        sig.initSign(((KeyStore.PrivateKeyEntry)getKeyStoreEntry(alias)).getPrivateKey());
        sig.update(message);
        return sig.sign();
    }

    /**
     * signing data with input crypto object
     * @param cryptoObject crypto object of signature
     * @param message      message to sign
     * @return signed message
     * @throws Exception cipher exception
     */
    public static byte[] signatureSHA256WithRSA(FingerprintManagerCompat.CryptoObject cryptoObject, byte[] message) throws Exception{
        Signature sig = cryptoObject.getSignature();
        sig.update(message);
        return sig.sign();
    }

    /**
     * verify signed data to SHA256WithRSA
     * @param alias     alias of keystore entry
     * @param signature signed data
     * @param message   message
     * @return verify result
     * @throws Exception cipher exception
     */
    public static boolean verifySHA256WithRSA(String alias, byte[] signature, byte[] message) throws Exception{
        Signature sig = Signature.getInstance("SHA256WithRSA");
        sig.update(message);
        sig.initVerify(((KeyStore.PrivateKeyEntry)getKeyStoreEntry(alias)).getCertificate().getPublicKey());
        return sig.verify(signature);
    }

    /**
     * verify signed data to SHA256WithRSA
     * @param cryptoObject crypto object of signature
     * @param signature    signed data
     * @param message      message
     * @return verify result
     * @throws Exception cipher exception
     */
    public static boolean verifySHA256WithRSA(FingerprintManagerCompat.CryptoObject cryptoObject, byte[] signature, byte[] message) throws Exception{
        Signature sig = cryptoObject.getSignature();
        sig.update(message);
        return sig.verify(signature);
    }

    /**
     * Sets default locale.
     */
    private static void setLocale(Context context, Locale locale) {
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.locale = locale;
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
