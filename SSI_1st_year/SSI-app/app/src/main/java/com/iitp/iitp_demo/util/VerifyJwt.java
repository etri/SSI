package com.iitp.iitp_demo.util;

import android.util.Base64;

import com.iitp.core.crypto.ECKeyUtils;
import com.iitp.iitp_demo.PublicKeyListener;
import com.iitp.iitp_demo.api.didResolver.DIDResolverAPI;
import com.iitp.iitp_demo.api.didResolver.model.DIDDocVo;
import com.iitp.iitp_demo.api.didResolver.model.PublicKeyVo;
import com.iitp.iitp_demo.api.model.ResponseVo;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.util.List;

import foundation.icon.did.exceptions.AlgorithmException;
import foundation.icon.did.jwt.IssuerDid;
import foundation.icon.did.jwt.Jwt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyJwt{
    private DIDResolverAPI didResolverAPI;
    public VerifyJwt(){
        didResolverAPI = DIDResolverAPI.getInstance();
    }

    public boolean checkVerify(ResponseVo responseVo, String publicKeyString){
        PrintLog.e("publicKeyString =" + publicKeyString);
        KeyFactory kf = null;
        try{

            kf = KeyFactory.getInstance("ECDSA", "BC");
        }catch(NoSuchProviderException e){
            PrintLog.e("VerifyJwt error");

        }catch(NoSuchAlgorithmException e){
            PrintLog.e("VerifyJwt error");
        }
        byte[] publicKeyDecode = Base64.decode(publicKeyString, Base64.NO_WRAP);
        PublicKey publicKey = ECKeyUtils.toECPublicKey(publicKeyDecode, "secp256k1");

        PrintLog.e("publicKeyDecode size=" + publicKeyDecode.length);
        Jwt jwt = Jwt.decode(responseVo.getResult().getCredential());
        Jwt.VerifyResult result = null;
        try{
            result = jwt.verify(publicKey);
        }catch(AlgorithmException e){
            PrintLog.e("VerifyJwt error");
        }
        PrintLog.e("verify result = " + result.isSuccess());
        return result.isSuccess();
    }

    public void getPublicKey(PublicKeyListener publicKeyListener, IssuerDid issuerDid){
        Call<DIDDocVo> rtn = didResolverAPI.didResolverAPIInfo.getDIDData(issuerDid.getDid());
        rtn.enqueue(new Callback<DIDDocVo>(){
            @Override
            public void onResponse(Call<DIDDocVo> call, Response<DIDDocVo> response){
                PrintLog.e("response code = " + response.code());
                if(response.code() == 200){
                    if(response.body() != null){

                        String publicKeyHex = null;
                        DIDDocVo didData = response.body();
                        List<PublicKeyVo> publicKeyVoList = didData.getDidDocument().getPublicKey();
                        for(PublicKeyVo key : publicKeyVoList){
                            if(key.getId().equals(issuerDid.getKeyId())){
                                publicKeyHex = key.getPublicKeyBase64();
                                PrintLog.e("pubkey = " + publicKeyHex);
                                publicKeyListener.requestComplete(publicKeyHex);
                            }
                        }
                    }
                }else{
                    publicKeyListener.error("error");
                }

            }

            @Override
            public void onFailure(Call<DIDDocVo> call, Throwable t){
                publicKeyListener.error(t.getMessage());
            }
        });
    }
}
