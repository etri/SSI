package com.bd.ssi.common;

import com.google.gson.Gson;
import com.metadium.did.MetadiumWallet;
import com.metadium.did.crypto.MetadiumKey;
import com.metadium.did.exception.DidException;
import com.metadium.did.protocol.MetaDelegator;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.interfaces.ECPrivateKey;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@Component
public class Utils {
    public Map<String, Object> getJsonToMap(String strJson){
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<String, Object>();
        map = gson.fromJson(strJson, map.getClass());

        return map;
    }

    public Map<String, Object> createDid() throws DidException, ParseException {
        // Create DID
        MetaDelegator delegator = new MetaDelegator("https://testdelegator.metadium.com", "https://testdelegator.metadium.com");
        MetadiumWallet wallet = MetadiumWallet.createDid(delegator);

        // Getter
        String did = wallet.getDid();	// Getting did
        String kid = wallet.getKid();  // Getting key id
        MetadiumKey key = wallet.getKey(); // Getting key
        BigInteger privateKey = wallet.getKey().getPrivateKey(); // Getting ec private key. bigint
        ECPrivateKey ecPrivateKey = wallet.getKey().getECPrivateKey(); // Getting ec private key. ECPrivateKey

        // serialize / deserialize
        String walletJson = wallet.toJson();
        MetadiumWallet newWallet = MetadiumWallet.fromJson(walletJson);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("did", did);
        map.put("kid", kid);
        map.put("prKey", privateKey);

        return map;
    }
}
