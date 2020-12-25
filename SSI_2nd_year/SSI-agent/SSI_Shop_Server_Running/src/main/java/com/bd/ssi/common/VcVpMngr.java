package com.bd.ssi.common;

import com.bd.ssi.common.metadium.MetadiumService;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.VerifiablePresentation;
import com.iitp.verifiable.VerifiableVerifier;
import com.iitp.verifiable.signer.MetadiumSigner;
import com.iitp.verifiable.util.ECKeyUtils;
import com.iitp.verifiable.verifier.MetadiumVerifier;
import com.metadium.did.MetadiumWallet;
import com.metadium.did.protocol.MetaDelegator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.interfaces.ECPrivateKey;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VcVpMngr {
    @Autowired
    MetadiumService metadiumService;

    private MetaDelegator delegator;

    public VcVpMngr(){
        delegator = new MetaDelegator("https://testdelegator.metadium.com", "https://api.metadium.com/dev");
    }

    /**
     * 물품DID, 제조사DID등 단순히 Metadium SDK를 이용하여 DID만 생성.
     */
    public String[] createDid(){
        MetadiumWallet wallet = null;
        try{
            wallet = MetadiumWallet.createDid(delegator);
        }catch(Exception ex){
            ex.printStackTrace();
        }

        String[] returnVal = {wallet.getDid(), wallet.toJson()};

        return returnVal;
    }


    //VC 생성
    public Map<String, String> doMakeVc(Map<String, String> user) {
        Map<String, String> signedMap = new HashMap<String, String>();

        // Create DID
        MetadiumWallet wallet = null;
        try{
            wallet = MetadiumWallet.createDid(delegator);
        }catch(Exception ex){
            ex.printStackTrace();
            signedMap.put("result", "unknown");
        }

        System.out.println("/* VC/VP 발급 */");

        String ISSUER_DID = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001bd1";
        String ISSUER_KID = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001bd1#MetaManagementKey#bae67d929f5758cceb8e43cdc6056eff7bbe4f92";
        ECPrivateKey ISSUER_PRIVATE_KEY = ECKeyUtils.toECPrivateKey(new BigInteger("5deae036a001840b80817cea7d8f0af503206f2749008c7ea3406395a480c8a0", 16), "secp256k1"); // PrivateKey load

//        String USER_DID = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001bd4";
//        String USER_KID = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001bd4#MetaManagementKey#f57c6a7655d4325dd10d55ab1854e4708eef5423";
        String USER_DID = wallet.getDid();
        String USER_KID = wallet.getKid();
        ECPrivateKey USER_PRIVATE_KEY = ECKeyUtils.toECPrivateKey(new BigInteger("16e9f408b4ef19c4fa7ff45b05fed96581ac938c6e05fc3695065197e36f4f18", 16), "secp256k1"); // PrivateKey load

        /* 만료일 */
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 2);

        /* VC 발급 - Issuer 가 발급 */
        VerifiableCredential vc = new VerifiableCredential();
        vc.setTypes(Arrays.asList("CREDENTIAL", "IdentificationCredential"));
        vc.setExpirationDate(calendar.getTime());
        vc.setIssuanceDate(new Date());
        Map<String, Object> subject = new HashMap<>();
        subject.put("id", USER_DID);    // 소유자 DID. 반드시 넣어야 함
        subject.put("name", user.get("name"));
        subject.put("birth_date", user.get("birth"));
        subject.put("address", user.get("address"));

        vc.setCredentialSubject(subject);
        String signedVc = "";
        try {
            signedVc = new MetadiumSigner(ISSUER_DID, ISSUER_KID, ISSUER_PRIVATE_KEY).sign(vc); // issuer 의 DID 로 서명
        }catch(Exception ex){
            ex.printStackTrace();
        }

        /* VP 생성 - 사용자가 VP 생성 */
        VerifiablePresentation vp = new VerifiablePresentation();
        vp.setTypes(Arrays.asList("PRESENTATION", "MyPresentation"));
        vp.addVerifiableCredential(signedVc);
        String signedVp = "";
        try {
            signedVp = new MetadiumSigner(USER_DID, USER_KID, USER_PRIVATE_KEY).sign(vp); // 사용자의 DID 로 서명
        }catch(Exception ex){
            ex.printStackTrace();
        }

        signedMap.put("did", USER_DID);
        signedMap.put("signedVc", signedVc);
        signedMap.put("signedVp", signedVp);
        signedMap.put("result", "true");

        System.out.println("### 발급된 DID:" + USER_DID);
        System.out.println("### 발급된 VC:" + signedVc);
        System.out.println("### 발급된 VPm:" + signedVp);

        return signedMap;
    }

    public Map<String, String> doVerifyVc(Map<String, String> signedMap){
        Map<String, String> returnMap = new HashMap<String, String>();

        /* VC/VP 검증 */
        System.out.println("/* VC/VP 검증 */");
        VerifiableVerifier.register("did:meta:", MetadiumVerifier.class);	// META
//        VerifiableVerifier.register("did:icon:", IconVerifier.class);		// ICON
        VerifiableVerifier.setResolverUrl("http://129.254.194.103:9000");

        /* VP 검증 */
        System.out.println("/* VP 검증 */");
        VerifiableVerifier verifiableVerifier = new VerifiableVerifier();
        VerifiablePresentation resultVp = null;
        try {
            resultVp = (VerifiablePresentation) verifiableVerifier.verify(signedMap.get("signedVp"));
        }catch(Exception ex){
            ex.printStackTrace();
        }

        /* VC 검증 */
        System.out.println("/* VC 검증 */");
        for (Object vcObject : resultVp.getVerifiableCredentials()) {
            if (vcObject instanceof String) {
                try {
                    VerifiableCredential resultVc = metadiumService.verifyVc(signedMap.get("signedVc"));

                    ArrayList<String> types = resultVc.getTypes().stream().collect(Collectors.toCollection(ArrayList::new));
//                    System.out.println("### types:" + types.get(1));

                    Map<String, Object> claims = (Map<String, Object>) resultVc.getCredentialSubject();
//                    System.out.println("### claims:" + claims.toString());

                    returnMap.put("vc", signedMap.get("signedVc"));
                    returnMap.put("vp", signedMap.get("signedVp"));
                    returnMap.put("result", "true");
                }catch(Exception ex){
                    ex.printStackTrace();
                    returnMap.put("result", "false");
                }
            }
        }

        return returnMap;
    }

    public boolean doVerifyVpOnly(String signedVp) {
        Map<String, String> returnMap = new HashMap<String, String>();

        /* Only VP 검증 */
        System.out.println("/* Only VP 검증 */");
        VerifiableVerifier.register("did:meta:", MetadiumVerifier.class);    // META
//        VerifiableVerifier.register("did:icon:", IconVerifier.class);		// ICON
        VerifiableVerifier.setResolverUrl("http://129.254.194.103:9000");

        /* VP 검증 */
        System.out.println("/* VP 검증 */");
        VerifiableVerifier verifiableVerifier = new VerifiableVerifier();

        try {
            //ZZZQQQ
//            VerifiablePresentation resultVp = (VerifiablePresentation) verifiableVerifier.verify(signedVp);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * 사전 VC 생성만을 위한 Method. VP는 생성하지 않음.
     */
    public Map<String, String> doMakePreVc(Map<String, String> user) {
        Map<String, String> signedMap = new HashMap<String, String>();

        // Create DID
        MetaDelegator delegator = new MetaDelegator("https://testdelegator.metadium.com", "https://testdelegator.metadium.com"); // set delegator, node url
        MetadiumWallet wallet = null;
        try{
            wallet = MetadiumWallet.createDid(delegator);
        }catch(Exception ex){
            ex.printStackTrace();
            signedMap.put("result", "unknown");
        }

//        String ISSUER_DID = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001bd1";
//        String ISSUER_KID = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001bd1#MetaManagementKey#bae67d929f5758cceb8e43cdc6056eff7bbe4f92";
//        ECPrivateKey ISSUER_PRIVATE_KEY = ECKeyUtils.toECPrivateKey(new BigInteger("5deae036a001840b80817cea7d8f0af503206f2749008c7ea3406395a480c8a0", 16), "secp256k1"); // PrivateKey load
        String ISSUER_DID = IssuerInfo.ISSUER_DID;
        String ISSUER_KID = IssuerInfo.ISSUER_KID;
        ECPrivateKey ISSUER_PRIVATE_KEY = ECKeyUtils.toECPrivateKey(new BigInteger(IssuerInfo.ISSUER_PRIVATEKEY, 16), "secp256k1"); // PrivateKey load

//        String USER_DID = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001bd4";
//        String USER_KID = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001bd4#MetaManagementKey#f57c6a7655d4325dd10d55ab1854e4708eef5423";
        String USER_DID = user.get("did");
//        String USER_KID = wallet.getKid();
//        ECPrivateKey USER_PRIVATE_KEY = ECKeyUtils.toECPrivateKey(new BigInteger("16e9f408b4ef19c4fa7ff45b05fed96581ac938c6e05fc3695065197e36f4f18", 16), "secp256k1"); // PrivateKey load

        /* 만료일 */
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 2);

        /* VC 발급 - Issuer 가 발급 */
        VerifiableCredential vc = new VerifiableCredential();
        vc.setTypes(Arrays.asList("CREDENTIAL", "LoginCredential"));
        vc.setExpirationDate(calendar.getTime());
        vc.setIssuanceDate(new Date());
        Map<String, Object> subject = new HashMap<>();
        subject.put("id", USER_DID);    // 소유자 DID. 반드시 넣어야 함
        subject.put("name", user.get("name"));
        subject.put("birth_date", user.get("birth_date"));
        subject.put("address", user.get("address"));
        subject.put("phone_num", user.get("phone_num"));

        vc.setCredentialSubject(subject);
        String signedVc = "";
        try {
            signedVc = new MetadiumSigner(ISSUER_DID, ISSUER_KID, ISSUER_PRIVATE_KEY).sign(vc); // issuer 의 DID 로 서명
        }catch(Exception ex){
            ex.printStackTrace();
        }

        /* VP 생성 - 사용자가 VP 생성 */
//        VerifiablePresentation vp = new VerifiablePresentation();
//        vp.setTypes(Arrays.asList("PRESENTATION", "MyPresentation"));
//        vp.addVerifiableCredential(signedVc);
//        String signedVp = "";
//        try {
//            signedVp = new MetadiumSigner(USER_DID, USER_KID, USER_PRIVATE_KEY).sign(vp); // 사용자의 DID 로 서명
//        }catch(Exception ex){
//            ex.printStackTrace();
//        }

        signedMap.put("did", USER_DID);
        signedMap.put("signedVc", signedVc);
//        signedMap.put("signedVp", signedVp);
        signedMap.put("result", "true");

//        System.out.println("### ISSUER_DID:" + ISSUER_DID);
//        System.out.println("### USER_DID:" + USER_DID);
//        System.out.println("### 발급된 VC:" + signedVc);
//        System.out.println("### 발급된 VPm:" + signedVp);

        return signedMap;
    }


    /**
     * parameter :
     *      pvc : 뭎품정보VC, dvc:거래증명VC
     * Desc: 물품정보VC, 거래증명VC를 받아 SSI APP ssi://vclist?vc= 에 전달하기 위한 json 생성.
     */
    public String getPvcDvc(String pvc, String dvc){
        String json =
                "{\"vc\":" +
                    "[\"" + pvc + "\"," +
                     "\"" + dvc + "\"" +
                     "]}";

        return json;
    }
}
