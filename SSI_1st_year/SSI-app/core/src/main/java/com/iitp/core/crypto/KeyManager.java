package com.iitp.core.crypto;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iitp.util.secure.SecureSharedPreferences;

import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.MnemonicUtils;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.web3j.crypto.Sign.publicKeyFromPrivate;

/**
 * Private key manager.<br>
 * Android keystore are used to securely manage private keys.<br>
 * <br>
 * <b>Generate key</b><br>
 * {@link KeyManager#generatePrivateKey(Context)}<br><br>
 * <b>Add key</b><br>
 * {@link KeyManager#addPrivateKey(Context, BigInteger, String)} Use when importing private key<br>
 * {@link KeyManager#addPrivateKey(Context, String, int[], String)} Use when importing mnemonic in MetaMask or Ledger<br>
 * {@link KeyManager#addPrivateKey(Context, String, String, String)} Use when importing keystore wallet<br><br>
 * <b>Get key</b><br>
 * <em>Do not use it because you can steal your key.</em><br>
 * {@link KeyManager#getPrivateKey(Context, String)}<br>
 * {@link KeyManager#getMnemonic(Context, String)}<br><br>
 * <b>Remove key</b><br>
 * {@link KeyManager#removePrivateKey(Context, String)}<br>
 * {@link KeyManager#clear(Context)}<br><br>
 * <b>Sign</b><br>
 * {@link KeyManager#signMessage(Context, String, byte[])}<br>
 * {@link KeyManager#signPrefixMessage(Context, String, byte[])}<br>
 * {@link KeyManager#signTransaction(Context, String, RawTransaction)}<br><br>
 * <b>Encrypt/Decrypt</b><br>
 * {@link KeyManager#encryptECIES(Context, String, byte[])}<br>
 * {@link KeyManager#decryptECIES(Context, String, byte[])}<br><br>
 */
public class KeyManager{
    private static final int HARDENED_BIT = 0x80000000;

    private static final String PREF_NAME = "Metadium.KeyManager";
    private static final String KEY_MNEMONIC_SUFFIX = "_mnemonic";

    private static final String PREF_INFO_NAME = "Metadium.KeyManager.Info";
    private static final String KEY_NAME_SUFFIX = "_name";
    private static final String KEY_TIMESTAMP_SUFFIX = "_timestamp";
    private static final String KEY_TYPE = "_type";

    public static final int KEY_TYPE_ERC_725 = 1;
    public static final int KEY_TYPE_SERVICE = 2;
    public static final int KEY_TYPE_WALLET = 9;

    /**
     * BIP44 ether, m/44'/60'/0'/0/0, Metamask, etc...
     **/
    private static final int[] BIP44_ETHER_PATH = {44 | HARDENED_BIT, 60 | HARDENED_BIT, HARDENED_BIT, 0, 0};
    /**
     * BIP44 Meta, m/44'/916'/0'/0/0, Only MetaApp
     **/
    private static final int[] BIP44_META_PATH = {44 | HARDENED_BIT, 916 | HARDENED_BIT, HARDENED_BIT, 0, 0};
    /**
     * BIP32 Ledger ether, m/44'/60'/0'/0
     */
    private static final int[] BIP32_ETHER_LEDGER_PATH = {44 | HARDENED_BIT, 60 | HARDENED_BIT, HARDENED_BIT, 0};
    /**
     * BIP44 testnet, m/44'/0'/0'/0/0
     **/
    private static final int[] BIP44_TESTNET_PATH = {44 | HARDENED_BIT, HARDENED_BIT, HARDENED_BIT, 0, 0};

    private static KeyManager instance;

    private SharedPreferences secureSharedPreferences;

    private SharedPreferences infoSharedPreferences;

    /**
     * singleton
     *
     * @return instance
     */
    public static synchronized KeyManager getInstance(){
        if(instance == null){
            instance = new KeyManager();
        }
        return instance;
    }

    public int[] getMetaPath(){
        int[] ret = null;
        if(BIP44_META_PATH != null){
            ret = new int[BIP44_META_PATH.length];
            for(int i = 0; i < BIP44_META_PATH.length; i++){
                ret[i] = BIP44_META_PATH[i];
            }
        }
        return ret;
    }

    public int[] getEtherPath(){
        int[] ret = null;
        if(BIP44_ETHER_PATH != null){
            ret = new int[BIP44_ETHER_PATH.length];
            for(int i = 0; i < BIP44_ETHER_PATH.length; i++){
                ret[i] = BIP44_ETHER_PATH[i];
            }
        }
        return ret;
    }

    public int[] getLedgerPath(){
        int[] ret = null;
        if(BIP32_ETHER_LEDGER_PATH != null){
            ret = new int[BIP32_ETHER_LEDGER_PATH.length];
            for(int i = 0; i < BIP32_ETHER_LEDGER_PATH.length; i++){
                ret[i] = BIP32_ETHER_LEDGER_PATH[i];
            }
        }
        return ret;
    }

    public int[] getTestnetPath(){

        int[] ret = null;
        if(BIP44_TESTNET_PATH != null){
            ret = new int[BIP44_TESTNET_PATH.length];
            for(int i = 0; i < BIP44_TESTNET_PATH.length; i++){
                ret[i] = BIP44_TESTNET_PATH[i];
            }
        }
        return ret;
    }

    private KeyManager(){
    }

    private SharedPreferences getSecureSharedPreferences(Context context){
        if(secureSharedPreferences == null){
            secureSharedPreferences = new SecureSharedPreferences(context, context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE));
        }
        return secureSharedPreferences;
    }

    private SharedPreferences getInfoSharedPreferences(Context context){
        if(infoSharedPreferences == null){
            infoSharedPreferences = context.getSharedPreferences(PREF_INFO_NAME, Context.MODE_PRIVATE);
        }
        return infoSharedPreferences;
    }

    private String addKey(Context context, BigInteger privateKey, String mnemonic, String name, int keyType) throws DuplicatedKeyException{
        String address = Numeric.prependHexPrefix(Keys.getAddress(ECKeyPair.create(privateKey).getPublicKey()));

        if(hasPrivateKey(context, address)){
            throw new DuplicatedKeyException("Duplicated key " + address);
        }

        SharedPreferences.Editor editor = getSecureSharedPreferences(context).edit();
        SharedPreferences.Editor infoEditor = getInfoSharedPreferences(context).edit();

        editor.putString(address, Base64.encodeToString(Numeric.toBytesPadded(privateKey, 32), Base64.NO_WRAP));
        if(MnemonicUtils.validateMnemonic(mnemonic)){
            editor.putString(address + KEY_MNEMONIC_SUFFIX, mnemonic);
        }
        if(name != null){
            infoEditor.putString(address + KEY_NAME_SUFFIX, name);
        }
        infoEditor.putInt(address + KEY_TYPE, keyType);
        infoEditor.putLong(address + KEY_TIMESTAMP_SUFFIX, System.currentTimeMillis());
        editor.apply();
        infoEditor.apply();

        return address;
    }

    /**
     * Add private key
     *
     * @param context    Android context
     * @param privateKey EC private key
     * @return address
     */
    public String addPrivateKey(Context context, BigInteger privateKey, String name) throws DuplicatedKeyException{
        return addKey(context, privateKey, null, name, KEY_TYPE_WALLET);
    }

    public String addPrivateKey(Context context, BigInteger privateKey, String name, int keyType) throws DuplicatedKeyException{
        return addKey(context, privateKey, null, name, keyType);
    }

    /**
     * Add private key with mnemonic<br>
     * If path is {@link KeyManager#BIP44_META_PATH}, saved mnemonic word<br>
     *
     * @param context  Android context
     * @param mnemonic mnemonic words
     * @param path     derived key path.
     * @return address
     */
    public String addPrivateKey(Context context, String mnemonic, int[] path, String name) throws DuplicatedKeyException{
        byte[] entropy = MnemonicUtils.generateEntropy(mnemonic);
        String normalizedMnemonic = MnemonicUtils.generateMnemonic(entropy);
        byte[] seed = MnemonicUtils.generateSeed(normalizedMnemonic, null);
        Bip32ECKeyPair master = Bip32ECKeyPair.generateKeyPair(seed);
        Bip32ECKeyPair keyPair = Bip32ECKeyPair.deriveKeyPair(master, path);
        boolean isMetaPath = Arrays.equals(path, BIP44_META_PATH);
        return addKey(context, keyPair.getPrivateKey(), isMetaPath ? normalizedMnemonic : null, name, isMetaPath ? KEY_TYPE_ERC_725 : KEY_TYPE_WALLET);
    }

    /**
     * Add private key with keystore wallet
     *
     * @param context  Android context
     * @param keystore Keystore json string
     * @param password Password of keystore
     * @return address
     */
    public String addPrivateKey(Context context, String keystore, String password, String name) throws DuplicatedKeyException, IOException, CipherException{
        ObjectMapper objectMapper = new ObjectMapper();
        WalletFile wallet = objectMapper.readValue(keystore, WalletFile.class);
        return addPrivateKey(context, Wallet.decrypt(password, wallet).getPrivateKey(), name);
    }

    /**
     * Generate private key. bip44 in {@link KeyManager#BIP44_META_PATH}
     *
     * @param context Android context
     * @return address
     */
    public String generatePrivateKey(Context context) throws DuplicatedKeyException{
        byte[] initialEntropy = new byte[16];
        new SecureRandom().nextBytes(initialEntropy);

        String mnemonic = MnemonicUtils.generateMnemonic(initialEntropy);

        return addPrivateKey(context, mnemonic, BIP44_META_PATH, null);
    }

    /**
     * Get private key<br>
     * <em>Do not use it because you can steal your key.</em><br>
     *
     * @param context Android context
     * @param address address to get private key
     * @return EC private key
     */
    public BigInteger getPrivateKey(Context context, String address){
        String pk = getSecureSharedPreferences(context).getString(address, null);
        return pk == null ? null : Numeric.toBigInt(Base64.decode(pk, Base64.NO_WRAP));
    }

    /**
     * Determine if a key exists for an address<br>
     *
     * @param context Android context
     * @param address address to determine private key
     * @return Existence
     */
    public boolean hasPrivateKey(Context context, String address){
        return getSecureSharedPreferences(context).getString(address, null) != null;
    }

    /**
     * Get mnemonic word<br>
     * <em>Do not use it because you can steal your key.</em><br>
     * The mnemonic exists only if {@link KeyManager#generatePrivateKey(Context)} or {@link KeyManager#addPrivateKey(Context, String, int[], String) with {@link KeyManager#BIP44_META_PATH } path}<br>
     *
     * @param context Android context
     * @param address to get private key
     * @return mnemonic word
     */
    public String getMnemonic(Context context, String address){
        return getSecureSharedPreferences(context).getString(address + KEY_MNEMONIC_SUFFIX, null);
    }

    public String getName(Context context, String address){
        return getInfoSharedPreferences(context).getString(address + KEY_NAME_SUFFIX, null);
    }

    public void setName(Context context, String address, String newName){
        getInfoSharedPreferences(context).edit().putString(address + KEY_NAME_SUFFIX, newName).apply();
    }

    public long getTimestamp(Context context, String address){
        return getInfoSharedPreferences(context).getLong(address + KEY_TIMESTAMP_SUFFIX, System.currentTimeMillis());
    }

    /**
     * Determine if a key make with mnemonic<br>
     *
     * @param context Android context
     * @param address to get private key
     * @return Existence mnemonic
     */
    public boolean withMnemonic(Context context, String address){
        return getSecureSharedPreferences(context).getString(address + KEY_MNEMONIC_SUFFIX, null) != null;
    }

    public int getKeyType(Context context, String address){
        int keyType = getInfoSharedPreferences(context).getInt(address + KEY_TYPE, -1);
        if(keyType == -1){
            return withMnemonic(context, address) ? KEY_TYPE_ERC_725 : KEY_TYPE_WALLET;
        }

        return keyType;
    }

    /**
     * Get addresses to manage
     *
     * @param context Android context
     * @return Address list
     */
    public List<String> getAddressList(Context context){
        List<String> addressList = new ArrayList<>();
        Set<String> keys = getSecureSharedPreferences(context).getAll().keySet();
        for(String key : keys){
            if(key.length() == 42){
                addressList.add(key);
            }
        }
        return addressList;
    }

    /**
     * Remove private key<br>
     *
     * @param context Android context
     * @param address to remove
     */
    public void removePrivateKey(Context context, String address){
        getSecureSharedPreferences(context).edit()
                .remove(address)
                .remove(address + KEY_MNEMONIC_SUFFIX)
                .apply();

        getInfoSharedPreferences(context).edit()
                .remove(address + KEY_NAME_SUFFIX)
                .remove(address + KEY_TIMESTAMP_SUFFIX)
                .apply();
    }

    /**
     * Clear private keys<br>
     *
     * @param context Android context
     */
    public void clear(Context context){
        getSecureSharedPreferences(context).edit().clear().apply();
        getInfoSharedPreferences(context).edit().clear().apply();
    }

    public static String signatureDataToString(Sign.SignatureData signatureData){
        ByteBuffer buffer = ByteBuffer.allocate(65);
        buffer.put(signatureData.getR());
        buffer.put(signatureData.getS());
        buffer.put(signatureData.getV());
        return Numeric.toHexString(buffer.array());
    }

    /**
     * signature hex string to {@link org.web3j.crypto.Sign.SignatureData}
     *
     * @param signature hex string of signature
     * @return signature object
     */
    public static Sign.SignatureData stringToSignatureData(String signature){
        byte[] bytes = Numeric.hexStringToByteArray(signature);
        return new Sign.SignatureData(bytes[64], Arrays.copyOfRange(bytes, 0, 32), Arrays.copyOfRange(bytes, 32, 64));
    }

    /**
     * Sign message<br>
     *
     * @param context Android context
     * @param address of private key to sign
     * @param message message to sign
     * @return signed message to hex string ('0x' prefixed), if not exists address return null
     */
    public String signMessage(Context context, String address, byte[] message){
        BigInteger privateKey = getPrivateKey(context, address);
        if(privateKey == null){
            return null;
        }
        return signatureDataToString(Sign.signMessage(message, ECKeyPair.create(privateKey)));
    }

    /**
     * Sign message with ethereum prefix<br>
     *
     * @param context Android context
     * @param address of private key to sign
     * @param message message to sign
     * @return signed message to hex string ('0x' prefixed), if not exists address return null
     */
    public String signPrefixMessage(Context context, String address, byte[] message){
        BigInteger privateKey = getPrivateKey(context, address);
        if(privateKey == null){
            return null;
        }
        return signatureDataToString(Sign.signPrefixedMessage(message, ECKeyPair.create(privateKey)));
    }

    /**
     * Sign transaction<br>
     *
     * @param context     Android context
     * @param address     of private key to sign
     * @param transaction to sign
     * @return signed transaction to hex string ('0x' prefixed), if not exists address return null
     */
    public String signTransaction(Context context, String address, RawTransaction transaction){
        BigInteger privateKey = getPrivateKey(context, address);
        if(privateKey == null){
            return null;
        }
        return Numeric.toHexString(TransactionEncoder.signMessage(transaction, Credentials.create(ECKeyPair.create(privateKey))));
    }

    /**
     * Decrypt message with ECIES<br>
     *
     * @param context          Android context
     * @param address          of private key to decrypt
     * @param encryptedMessage message to decrypt
     * @return decrypted message, if not exists address return null
     */
    public byte[] decryptECIES(Context context, String address, byte[] encryptedMessage) throws GeneralSecurityException{
        BigInteger privateKey = getPrivateKey(context, address);
        if(privateKey == null){
            return null;
        }
        return ECIES.decrypt(privateKey, encryptedMessage);
    }

    /**
     * Encrypt message with ECIES<br>
     *
     * @param context Android context
     * @param address of public key to encrypt
     * @param message message to encrypt
     * @return encrypted message, if not exists address return null
     */
    public byte[] encryptECIES(Context context, String address, byte[] message) throws GeneralSecurityException{
        BigInteger privateKey = getPrivateKey(context, address);
        if(privateKey == null){
            return null;
        }
        return ECIES.encrypt(publicKeyFromPrivate(privateKey), message);
    }


    public String getPublicKey(Context context, String address){
        BigInteger privateKey = getPrivateKey(context, address);
        if(privateKey == null){
            return null;
        }
        return Numeric.toHexStringWithPrefix(publicKeyFromPrivate(privateKey));
    }
}
