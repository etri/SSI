package com.iitp.iitp_demo.util;

import android.content.Context;

import androidx.annotation.NonNull;

import com.iitp.core.crypto.KeyManager;
import com.iitp.core.identity.Identity;
import com.iitp.iitp_demo.BuildConfig;
import com.iitp.iitp_demo.data.IdentityStore;
import com.iitp.iitp_demo.model.DidVo;

import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.List;

/**
 * {@link KeyManager} 의 utility
 */
public class KeyManagerUtil{
    /**
     * Address information
     */
    public static class AddressInfo implements Comparable<AddressInfo>{
        /**
         * address of key
         */
        String address;
        /**
         * 사용자가 지정한 이름. 없으면 null
         */
        String name;
        /**
         * 니모닉으로 만들어졌는지 여부
         */
        boolean withMnemonic;
        /**
         * 생성 시간
         */
        long timestamp;

        @Override
        public int compareTo(@NonNull AddressInfo o){
            if(this.withMnemonic == o.withMnemonic){
                return (int) (this.timestamp - o.timestamp);
            }else{
                return this.withMnemonic ? 1 : -1;
            }
        }

        public String getAddress(){
            return address;
        }

        public String getName(){
            return name;
        }

        public boolean isWithMnemonic(){
            return withMnemonic;
        }
    }

    /**
     * Get associated(management) address. 앱 초기에 니모닉으로 만들어진 Key 의 address
     *
     * @param context android context
     * @return address. 없으면 null 반환
     */
    public static String getManagementAddress(Context context){
        KeyManager keyManager = KeyManager.getInstance();

        List<String> addressList = keyManager.getAddressList(context);
        for(String address : addressList){
            PrintLog.e("Address list = "+address);
            if(keyManager.withMnemonic(context, address)){
                return address;
            }
        }
        return null;
    }

    /**
     * get KeyId
     *
     * @param ctx
     * @return KeyId
     */
    public static String getKeyId(Context ctx){
        String net = BuildConfig.MAIN_NET ? "" : "testnet:";
        Identity identity = IdentityStore.loadIdentity(ctx);
        KeyManager keyManager = KeyManager.getInstance();
        String address = getManagementAddress(ctx);
        PrintLog.e("address = " + address);
        String privateKey = Numeric.toHexStringNoPrefixZeroPadded(keyManager.getPrivateKey(ctx, address), 64);
        PrintLog.e("privateKey = " + privateKey);
        String publicKey = keyManager.getPublicKey(ctx, address);
        PrintLog.e("publicKey = " + publicKey);
        String metaId = identity.getMetaId();
        PrintLog.e("metaId = " + metaId);
        BigInteger metaBig = Numeric.toBigInt(metaId);
        String metaIdOrg = Numeric.toHexStringNoPrefixZeroPadded(metaBig, 64);
        PrintLog.e("metaIdOrg = " + metaIdOrg);
        String iss = "did:meta:" + net + metaIdOrg.replace("0x", "");
        String keyId = "MetaManagementKey#" + address.replace("0x", "");
        PrintLog.e("keyId = " + keyId);
        return keyId;
    }

    public static String getMetaId(Context ctx){
        String net = BuildConfig.MAIN_NET ? "" : "testnet:";
        Identity identity = IdentityStore.loadIdentity(ctx);
        KeyManager keyManager = KeyManager.getInstance();
        String address = getManagementAddress(ctx);
        PrintLog.e("address = " + address);
        String privateKey = Numeric.toHexStringNoPrefixZeroPadded(keyManager.getPrivateKey(ctx, address), 64);
        PrintLog.e("privateKey = " + privateKey);
        String publicKey = keyManager.getPublicKey(ctx, address);
        PrintLog.e("publicKey = " + publicKey);
        String metaId = identity.getMetaId();
        PrintLog.e("metaId = " + metaId);
        BigInteger metaBig = Numeric.toBigInt(metaId);
        String metaIdOrg = Numeric.toHexStringNoPrefixZeroPadded(metaBig, 32);
        PrintLog.e("metaIdOrg = " + metaIdOrg);
        String iss = "did:meta:" + net + metaIdOrg.replace("0x", "");
//        String keyId = iss + "#MetaManagementKey#" + address.replace("0x", "");
//        PrintLog.e("keyId = " + keyId);
        return iss;
    }

    public static String getDid(Context ctx){
        String net = BuildConfig.MAIN_NET ? "" : "testnet:";
        Identity identity = IdentityStore.loadIdentity(ctx);
        KeyManager keyManager = KeyManager.getInstance();
        String address = getManagementAddress(ctx);
        PrintLog.e("address = " + address);
        String iss = null;
        if(address != null){
            String privateKey = Numeric.toHexStringNoPrefixZeroPadded(keyManager.getPrivateKey(ctx, address), 64);
            PrintLog.e("privateKey = " + privateKey);
            String publicKey = keyManager.getPublicKey(ctx, address);
            PrintLog.e("publicKey = " + publicKey);
            String metaId = identity.getMetaId();
            PrintLog.e("metaId = " + metaId);
            BigInteger metaBig = Numeric.toBigInt(metaId);
            String metaIdOrg = Numeric.toHexStringNoPrefixZeroPadded(metaBig, 64);
            PrintLog.e("metaIdOrg = " + metaIdOrg);
            iss = "did:meta:" + net + metaIdOrg.replace("0x", "");
            PrintLog.e("iss = " + iss);
        }
        return iss;
    }

    public static DidVo getDidVo(Context ctx){
        DidVo did = new DidVo();
        String net = BuildConfig.MAIN_NET ? "" : "testnet:";
        Identity identity = IdentityStore.loadIdentity(ctx);
        KeyManager keyManager = KeyManager.getInstance();
        String address = getManagementAddress(ctx);
        PrintLog.e("address = " + address);
        String iss = null;
        if(address != null){
            String privateKey = Numeric.toHexStringNoPrefixZeroPadded(keyManager.getPrivateKey(ctx, address), 64);
            PrintLog.e("privateKey = " + privateKey);
            String publicKey = keyManager.getPublicKey(ctx, address);
            PrintLog.e("publicKey = " + publicKey);
            String metaId = identity.getMetaId();
            PrintLog.e("metaId = " + metaId);
            BigInteger metaBig = Numeric.toBigInt(metaId);
            String metaIdOrg = Numeric.toHexStringNoPrefixZeroPadded(metaBig, 64);
            PrintLog.e("metaIdOrg = " + metaIdOrg);
            iss = "did:meta:" + net + metaIdOrg.replace("0x", "");
            PrintLog.e("iss = " + iss);

            did.setPrivateKey(privateKey);
            did.setPublicKey(publicKey);
            did.setMetaId(metaId);
            did.setMetaIdOrg(metaIdOrg);
            did.setAddress(address);
            did.setIss(iss);
        }
        return did;
    }
}
