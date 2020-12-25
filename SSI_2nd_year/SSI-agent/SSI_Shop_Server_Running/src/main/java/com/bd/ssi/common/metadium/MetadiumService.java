package com.bd.ssi.common.metadium;

import com.bd.ssi.common.IssuerInfo;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.VerifiablePresentation;
import com.iitp.verifiable.VerifiableVerifier;
import com.iitp.verifiable.signer.MetadiumSigner;
import com.iitp.verifiable.util.ECKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.interfaces.ECPrivateKey;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class MetadiumService {
    public static final String TESTNET_URL = "https://testnetresolver.metadium.com";

//    public static final String ISSUER_DID_TEST = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001bd1";
//    public static final String ISSUER_KID_TEST = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001bd1#MetaManagementKey#bae67d929f5758cceb8e43cdc6056eff7bbe4f92";
//    public static final String ISSUER_PRIVATE_KEY_TEST = "5deae036a001840b80817cea7d8f0af503206f2749008c7ea3406395a480c8a0";
    public static final String ISSUER_DID_TEST = IssuerInfo.ISSUER_DID;
    public static final String ISSUER_KID_TEST = IssuerInfo.ISSUER_KID;
    public static final String ISSUER_PRIVATE_KEY_TEST = IssuerInfo.ISSUER_PRIVATEKEY;

    @Autowired
    VerifiableVerifier verifiableVerifier;

    public ECPrivateKey getIssuerPrivateKey(){
        return ECKeyUtils.toECPrivateKey(new BigInteger(ISSUER_PRIVATE_KEY_TEST, 16), "secp256k1"); // PrivateKey load
    }

    public String getType(VerifiableCredential vc){
        ArrayList<String> types = vc.getTypes().stream().collect(Collectors.toCollection(ArrayList::new));
        return types.get(1);
    }

    public String signVc(VerifiableCredential vc) throws Exception {
        return new MetadiumSigner(ISSUER_DID_TEST, ISSUER_KID_TEST, getIssuerPrivateKey()).sign(vc); // issuer 의 DID 로 서명
    }
    public String signVp(VerifiablePresentation vp) throws Exception {
        return new MetadiumSigner(ISSUER_DID_TEST, ISSUER_KID_TEST, getIssuerPrivateKey()).sign(vp);
    }
    public VerifiablePresentation verifyVp(String signedVp) throws Exception {
        return (VerifiablePresentation)verifiableVerifier.verify(signedVp);
    }
    public VerifiableCredential verifyVc(String signedVc) throws Exception {
        return (VerifiableCredential)verifiableVerifier.verify(signedVc);
    }

}
