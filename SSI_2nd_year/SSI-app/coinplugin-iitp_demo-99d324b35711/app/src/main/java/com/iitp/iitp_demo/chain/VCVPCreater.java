package com.iitp.iitp_demo.chain;

import android.content.Context;
import android.icu.util.Calendar;

import com.iitp.core.crypto.KeyManager;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.VerifiablePresentation;
import com.iitp.verifiable.VerifiableVerifier;
import com.iitp.verifiable.signer.MetadiumSigner;
import com.iitp.verifiable.util.ECKeyUtils;
import com.iitp.verifiable.verifier.IconVerifier;
import com.iitp.verifiable.verifier.MetadiumVerifier;
import com.metadium.did.MetadiumWallet;
import com.metadium.did.crypto.MetadiumKey;
import com.metadium.did.exception.DidException;
import com.metadium.did.protocol.MetaDelegator;

import org.bouncycastle.util.encoders.Hex;
import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.MnemonicUtils;

import java.math.BigInteger;
import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import foundation.icon.did.Credential;
import foundation.icon.did.core.Algorithm;
import foundation.icon.did.core.AlgorithmProvider;
import foundation.icon.did.core.DidKeyHolder;
import foundation.icon.did.document.EncodeType;
import foundation.icon.did.exceptions.AlgorithmException;
import foundation.icon.did.exceptions.KeyPairException;
import foundation.icon.did.protocol.ClaimRequest;
import foundation.icon.icx.data.Bytes;

import static com.iitp.core.crypto.KeyManager.BIP44_META_PATH;


public class VCVPCreater{


    private static VCVPCreater instance;
    private String ISSUER_DID = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000000382";
    private String ISSUER_KID = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000000382#MetaManagementKey#59ddc27f5bc6983458eac013b1e771d11c908683";
    private ECPrivateKey ISSUER_PRIVATE_KEY = ECKeyUtils.toECPrivateKey(new BigInteger("fdcdca38d0c62f3564f90afdc4c04c1f936b9edf95b5d8841a70b40cc84cfd90", 16), "secp256k1"); // PrivateKey load

    //    private String USER_DID = "did:meta:testnet:000000000000000000000000000000000000000000000000000000000000054b";
//    private String USER_KID = "did:meta:testnet:000000000000000000000000000000000000000000000000000000000000054b#MetaManagementKey#cfd31afff25b2260ea15ef59f2d5d7dfe8c13511";
//    private ECPrivateKey USER_PRIVATE_KEY = ECKeyUtils.toECPrivateKey(new BigInteger("86975dca6a36062768cf4b648b5b3f712caa2d1d61fa42520624a8e574788822", 16), "secp256k1"); // PrivateKey load
    private MetadiumWallet wallet = null;

    private String sampleDID = null;

    //    public static String NoneZKPStoreAddress = "none_zkp_store_address";
    public static final String NoneZKPStoreCardTokenKorea = "none_zkp_store_cardtoken_korea";
    public static final String NoneZKPStoreCardTokenSeoul = "none_zkp_store_cardtoken_seoul";
    public static final String NoneZKPStoreIdCard = "none_zkp_store_idcard";
    public static final String NoneZKPStoreMobile = "none_zkp_store_Mobile";
    public static final String NoneZKPStoreStock = "none_zkp_store_stock";
    public static final String NoneZKPStoreLogin = "none_zkp_store_login";
    public static final String NoneZKPStorePost = "none_zkp_store_post";
    public static final String NoneZKPStoreDelegationToken = "none_zkp_store_delegationToken";
    public static final String ZKP_IdentificationCredential = "ZKP_IdentificationCredential";
    public static final String ZKP_UniCredential = "ZKP_UniCredential";
    public static final String OfficeCredential = "OfficeCredential";

    public static final String DELEGATOR_VC = "delegated_id_VC";
    public static final String ProductProofCredential = "ProductProofCredential";
    public static final String ProductCredential = "ProductCredential";
    private Context ctx;


    private VCVPCreater(){

    }

    public static synchronized VCVPCreater getInstance(){
        if(instance == null){
            instance = new VCVPCreater();
        }
        return instance;
    }

    private void vcCreateIDCard(){
        String vcJwt = null;
        // 만료일
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 2);
        // VC 발급 - Issuer 가 발급
        VerifiableCredential vc = new VerifiableCredential();
        vc.setTypes(Arrays.asList("CREDENTIAL", "IdentificationCredential"));
        vc.setExpirationDate(calendar.getTime());
        vc.setIssuanceDate(new Date());
        Map<String, Object> subject = new HashMap<>();
        subject.put("id", sampleDID);    // 소유자 DID. 반드시 넣어야 함
        subject.put("name", "Gil-dong Hong");
        subject.put("birth_date", "1988-07-21");
        subject.put("address", "218 Gajeong-ro, Yuseong-gu, Daejeon, 34129, KOREA");
        vc.setCredentialSubject(subject);
        try{
            String signedVc = new MetadiumSigner(ISSUER_DID, ISSUER_KID, ISSUER_PRIVATE_KEY).sign(vc); // issuer 의 DID 로 서명
            vcJwt = signedVc;
            PrintLog.e("VC = " + signedVc);
        }catch(Exception e){
            PrintLog.e("vcCreateIDCard error");
        }
        savePreVCList(NoneZKPStoreIdCard, vcJwt);

    }

    private void vcCreateProduct(){
        String vcJwt = null;
        // 만료일
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 2);
        // VC 발급 - Issuer 가 발급
        VerifiableCredential vc = new VerifiableCredential();
        vc.setTypes(Arrays.asList("CREDENTIAL", "ProductCredential"));
        vc.setExpirationDate(calendar.getTime());
        vc.setIssuanceDate(new Date());
        Map<String, Object> subject = new HashMap<>();
        subject.put("id", sampleDID);    //todo 물품DID
        subject.put("name", "product name");
        subject.put("SN", "erial number");
        subject.put("production_date", "2020-07-21");
        vc.setCredentialSubject(subject);
        try{
            String signedVc = new MetadiumSigner(ISSUER_DID, ISSUER_KID, ISSUER_PRIVATE_KEY).sign(vc); // issuer 의 DID 로 서명
            vcJwt = signedVc;
            PrintLog.e("VC = " + signedVc);
        }catch(Exception e){
            PrintLog.e("vcCreateProduct error");
        }
        savePreVCList(ProductCredential, vcJwt);
    }

    public String vcCreateLogin(){
        String vcJwt = null;
        // 만료일
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 2);
        // VC 발급 - Issuer 가 발급
        VerifiableCredential vc = new VerifiableCredential();
        vc.setTypes(Arrays.asList("CREDENTIAL", "LoginCredential"));
        vc.setExpirationDate(calendar.getTime());
        vc.setIssuanceDate(new Date());
        Map<String, Object> subject = new HashMap<>();
        subject.put("id", sampleDID);
        subject.put("name", "Gil-dong Hong");
        subject.put("age", "30");
        subject.put("address", "218 Gajeong-ro, Yuseong-gu, Daejeon, 34129, KOREA");
        vc.setCredentialSubject(subject);
        try{
            String signedVc = new MetadiumSigner(ISSUER_DID, ISSUER_KID, ISSUER_PRIVATE_KEY).sign(vc); // issuer 의 DID 로 서명
            vcJwt = signedVc;
            PrintLog.e("VC = " + signedVc);
        }catch(Exception e){
            PrintLog.e("vcCreateLogin error");
        }
        savePreVCList(NoneZKPStoreLogin, vcJwt);
        return vcJwt;
    }

//    public void vcCreateCardToken(String did){
//        String vcJwt = null;
//        // 만료일
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.YEAR, 2);
//        // VC 발급 - Issuer 가 발급
//        VerifiableCredential vc = new VerifiableCredential();
//        vc.setTypes(Arrays.asList("CREDENTIAL", "CardTokenCredential"));
//        vc.setExpirationDate(calendar.getTime());
//        vc.setIssuanceDate(new Date());
//        Map<String, Object> subject = new HashMap<>();
//        subject.put("id", did);
//        subject.put("Token", "Token");
//        vc.setCredentialSubject(subject);
//        try{
//            String signedVc = new MetadiumSigner(ISSUER_DID, ISSUER_KID, ISSUER_PRIVATE_KEY).sign(vc); // issuer 의 DID 로 서명
//            vcJwt = signedVc;
//            PrintLog.e("VC = " + signedVc);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        savePreVCList(NoneZKPStoreCardToken, vcJwt);
//    }

    private void vcCreateMobile(){
        String vcJwt = null;
        // 만료일
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 2);
        // VC 발급 - Issuer 가 발급
        VerifiableCredential vc = new VerifiableCredential();
        vc.setTypes(Arrays.asList("CREDENTIAL", "PhoneCredential"));
        vc.setExpirationDate(calendar.getTime());
        vc.setIssuanceDate(new Date());
        Map<String, Object> subject = new HashMap<>();
        subject.put("id", sampleDID);
        subject.put("name", "Gil-dong Hong");
        subject.put("phone_num", "010-1234-5678");
        vc.setCredentialSubject(subject);
        try{
            String signedVc = new MetadiumSigner(ISSUER_DID, ISSUER_KID, ISSUER_PRIVATE_KEY).sign(vc); // issuer 의 DID 로 서명
            vcJwt = signedVc;
            PrintLog.e("VC = " + signedVc);
        }catch(Exception e){
            PrintLog.e("vcCreateMobile error");
        }
        savePreVCList(NoneZKPStoreMobile, vcJwt);
    }

    private void vcCreateAddress(){
        String vcJwt = null;
        // 만료일
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 2);
        // VC 발급 - Issuer 가 발급
        VerifiableCredential vc = new VerifiableCredential();
        vc.setTypes(Arrays.asList("CREDENTIAL", "AddressCredential"));
        vc.setExpirationDate(calendar.getTime());
        vc.setIssuanceDate(new Date());
        Map<String, Object> subject = new HashMap<>();
        subject.put("id", sampleDID);
        subject.put("name", "Gil-dong Hong");
        subject.put("address", "218 Gajeong-ro, Yuseong-gu, Daejeon, 34129, KOREA");
        vc.setCredentialSubject(subject);
        try{
            String signedVc = new MetadiumSigner(ISSUER_DID, ISSUER_KID, ISSUER_PRIVATE_KEY).sign(vc); // issuer 의 DID 로 서명
            vcJwt = signedVc;
            PrintLog.e("VC = " + signedVc);
        }catch(Exception e){
            PrintLog.e("vcCreateAddress error");
        }
        savePreVCList(NoneZKPStorePost, vcJwt);
    }

    private void vcCreateStock(){
        String vcJwt = null;
        // 만료일
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 2);
        // VC 발급 - Issuer 가 발급
        VerifiableCredential vc = new VerifiableCredential();
        vc.setTypes(Arrays.asList("CREDENTIAL", "StockServiceCredential"));
        vc.setExpirationDate(calendar.getTime());
        vc.setIssuanceDate(new Date());
        Map<String, Object> subject = new HashMap<>();
        subject.put("id", sampleDID);
        subject.put("name", "Gil-dong Hong");
        subject.put("register_id", "등록된 사용자 id");
        subject.put("start_date", "2011-01-02");
        subject.put("address", "218 Gajeong-ro, Yuseong-gu, Daejeon, 34129, KOREA");
        vc.setCredentialSubject(subject);
        try{
            String signedVc = new MetadiumSigner(ISSUER_DID, ISSUER_KID, ISSUER_PRIVATE_KEY).sign(vc); // issuer 의 DID 로 서명
            vcJwt = signedVc;
            PrintLog.e("VC = " + signedVc);
        }catch(Exception e){
            PrintLog.e("vcCreateStock error");
        }
        savePreVCList(NoneZKPStoreStock, vcJwt);
    }

    public String vcCreateDelegationToken(Context context, String did, String payment, String delegatorDID, String delegated_attr){
        String vcJwt = null;
        // 만료일
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 2);
        // VC 발급 - Issuer 가 발급
        VerifiableCredential vc = new VerifiableCredential();
        vc.setTypes(Arrays.asList("CREDENTIAL", "DelegatedVC"));
        vc.setExpirationDate(calendar.getTime());
        vc.setIssuanceDate(new Date());
        Map<String, Object> subject = new HashMap<>();
        subject.put("payment", payment);
        subject.put("DID_delegator", delegatorDID);
        subject.put("type", delegated_attr);
        vc.setCredentialSubject(subject);
        KeyManager keyManager = new KeyManager(did);
        String kid = keyManager.getManagementKeyId(context);
        BigInteger privatekey = keyManager.getPrivate(context);
        ECPrivateKey ecPrivateKey = ECKeyUtils.toECPrivateKey(privatekey, "secp256k1");
        try{
            String signedVc = new MetadiumSigner(did, kid, ecPrivateKey).sign(vc); // issuer 의 DID 로 서명
            vcJwt = signedVc;
            PrintLog.e("VC = " + signedVc);
        }catch(Exception e){
            PrintLog.e("vcCreateDelegationToken error");
        }
        return vcJwt;
    }

    public String vcCreateDelegationIdCard(Context context, String did, String delegatorDID, String vrifierDid){
        String vcJwt = null;
        // 만료일
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 2);
        // VC 발급 - Issuer 가 발급
        VerifiableCredential vc = new VerifiableCredential();
        vc.setTypes(Arrays.asList("CREDENTIAL", "DelegatedVC"));
        vc.setExpirationDate(calendar.getTime());
        vc.setIssuanceDate(new Date());
        Map<String, Object> subject = new HashMap<>();
        subject.put("DID_delegator", delegatorDID); //위임받는 사
        subject.put("verifier", vrifierDid);
        subject.put("type", "IdentificationCredential");
        vc.setCredentialSubject(subject);
        KeyManager keyManager = new KeyManager(did);
        String keyid = keyManager.getManagementKeyId(context);
        ECPrivateKey privateKey = ECKeyUtils.toECPrivateKey(keyManager.getPrivate(context), "secp256k1"); // PrivateKey load
        try{
            String signedVc = new MetadiumSigner(did, keyid, privateKey).sign(vc);
            vcJwt = signedVc;
            PrintLog.e("VC = " + signedVc);
        }catch(Exception e){
            PrintLog.e("vcCreateDelegationIdCard error");
        }
        return vcJwt;
    }

    public void makePreNonZkpVC(Context context, String metadid){
        sampleDID = metadid;
        ctx = context;
        vcCreateAddress();
//        vcCreateCardToken(sampleDID);
        vcCreateIDCard();
        vcCreateMobile();
        vcCreateProduct();
        vcCreateStock();
        vcCreateLogin();
    }


    public String vcCreate(){
        String vcJwt = null;
        // 만료일
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 2);
        // VC 발급 - Issuer 가 발급
        VerifiableCredential vc = new VerifiableCredential();
        vc.setTypes(Arrays.asList("CREDENTIAL", "IdentificationCredential"));
        vc.setExpirationDate(calendar.getTime());
        vc.setIssuanceDate(new Date());
        Map<String, Object> subject = new HashMap<>();
        subject.put("id", sampleDID);    // 소유자 DID. 반드시 넣어야 함
        subject.put("name", "Gil-dong Hong");
        subject.put("birth_date", "1988-07-21");
        subject.put("address", "218 Gajeong-ro, Yuseong-gu, Daejeon, 34129, KOREA");
        vc.setCredentialSubject(subject);
        try{
            String signedVc = new MetadiumSigner(ISSUER_DID, ISSUER_KID, ISSUER_PRIVATE_KEY).sign(vc); // issuer 의 DID 로 서명
            vcJwt = signedVc;
            PrintLog.e("VC = " + signedVc);
        }catch(Exception e){
            PrintLog.e("vcCreate error");
        }
        return vcJwt;
    }

    private VerifiableCredential vcCreateTest(){
        String vcJwt = null;
        // 만료일
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 2);
        // VC 발급 - Issuer 가 발급
        VerifiableCredential vc = new VerifiableCredential();
        vc.setTypes(Arrays.asList("CREDENTIAL", "IdentificationCredential"));
        vc.setExpirationDate(calendar.getTime());
        vc.setIssuanceDate(new Date());
        Map<String, Object> subject = new HashMap<>();
        subject.put("id", sampleDID);    // 소유자 DID. 반드시 넣어야 함
        subject.put("name", "Gil-dong Hong");
        subject.put("birth_date", "1988-07-21");
        subject.put("address", "218 Gajeong-ro, Yuseong-gu, Daejeon, 34129, KOREA");
        vc.setCredentialSubject(subject);
        return vc;
    }

//    public String vcCreateProductionCredential(DidDataVo didDataVo, String id, String sn, String company, String production, String productionDate){
//        String vcJwt = null;
//        ECKeyPair ecKeyPair = getManagementECKeyPair(didDataVo.getMnemonic());
//        MetadiumKey newKey = new MetadiumKey(ecKeyPair);
//        MetadiumWallet sellerWallet = new MetadiumWallet(didDataVo.getDid(), newKey);
//        // 만료일
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.YEAR, 2);
//        // VC 발급 - Issuer 가 발급
//        VerifiableCredential vc = new VerifiableCredential();
//        vc.setTypes(Arrays.asList("VerifiableCredential", "ProductCredential"));
//        vc.setExpirationDate(calendar.getTime());
//        vc.setIssuanceDate(new Date());
//        Map<String, Object> subject = new HashMap<>();
//        subject.put("id", id);
//        subject.put("SN", sn);
//        subject.put("company", company);
//        subject.put("production", production);
//        subject.put("production_date", productionDate);
//        subject.put("iit", calendar.getTime());
//        vc.setCredentialSubject(subject);
//        try{
//            String signedVc = new MetadiumSigner(sellerWallet.getDid(), sellerWallet.getKid(), sellerWallet.getKey().getECPrivateKey()).sign(vc); // issuer 의 DID 로 서명
//            vcJwt = signedVc;
//            PrintLog.e("VC = " + signedVc);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return vcJwt;
//    }


    public String vpCreate(String userDID, String userKid, ECPrivateKey userPrivateKey, List<String> vcList){

        String VP = null;
        // VP 생성 - 사용자가 VP 생성
        VerifiablePresentation vp = new VerifiablePresentation();
        vp.setTypes(Arrays.asList("PRESENTATION", "MyPresentation"));
        for(String vcData : vcList){
            vp.addVerifiableCredential(vcData);
        }
        String signedVp = null; // 사용자의 DID 로 서명
        try{
            signedVp = new MetadiumSigner(userDID, userKid, userPrivateKey).sign(vp);
        }catch(Exception e){
            PrintLog.e("vpCreate error");
        }
        return signedVp;
    }

    public void testVCResign(){
        // Set verifier
        VerifiableVerifier.register("did:meta:", MetadiumVerifier.class);    // META
        VerifiableVerifier.register("did:icon:", IconVerifier.class);        // ICON
//        VerifiableVerifier.setResolverUrl("http://129.254.194.103:9000");
        VerifiableVerifier.setResolverUrl(" http://129.254.194.113:80");


// Metadium DID 생성
        MetaDelegator delegator = new MetaDelegator("https://testdelegator.metadium.com", "https://testdelegator.metadium.com");

        new Thread(() -> {
            try{
                wallet = MetadiumWallet.createDid(delegator);
            }catch(DidException e){
                e.printStackTrace();
            }
            // Metadium DID로 VC 생성
            VerifiableCredential vc = new VerifiableCredential();
            vc.setTypes(Arrays.asList("CREDENTIAL", "IdentificationCredential"));
            vc.setIssuanceDate(new Date());
            Map<String, Object> subject = new HashMap<>();
            subject.put("id", wallet.getDid());    // 소유자 DID. 반드시 넣어야 함
            subject.put("name", "product name");
            subject.put("SN", "serial name");
            subject.put("production_date", "2020-07-21");
            vc.setCredentialSubject(subject);
            String signedVc = null; // DID 로 서명
            try{
                signedVc = new MetadiumSigner(wallet.getDid(), wallet.getKid(), wallet.getKey().getECPrivateKey()).sign(vc);
                PrintLog.e("signedVC = " + signedVc);
            }catch(Exception e){
                e.printStackTrace();
            }

// VC 검증
            VerifiableVerifier verifiableVerifier = new VerifiableVerifier();
            try{
                VerifiableCredential resultVc = (VerifiableCredential) verifiableVerifier.verify(signedVc);
            }catch(Exception e){
                e.printStackTrace();
            }

// Metadium DID 키 변경
            try{
                wallet.updateKeyOfDid(delegator, new MetadiumKey());
            }catch(DidException e){
                e.printStackTrace();
            }catch(InvalidAlgorithmParameterException e){
                e.printStackTrace();
            }
            try{
                String signedVc2 = new MetadiumSigner(wallet.getDid(), wallet.getKid(), wallet.getKey().getECPrivateKey()).sign(vc); // 키가 변경된 DID 로 재서명
            }catch(Exception e){
                e.printStackTrace();
            }

// Metadium DID로 재서명 된 VC 검증
            try{
                String resignedVc = new MetadiumSigner(wallet.getDid(), wallet.getKid(), wallet.getKey().getECPrivateKey()).sign(vc); // DID 로 서명
            }catch(Exception e){
                e.printStackTrace();
            }
            PrintLog.e("finish test");
        }).start();

    }

    private void savePreVCList(String key, String jwt){
        PrintLog.e("VC = " + jwt);
        PrintLog.e("save Key = " + key);
        CommonPreference.getInstance(ctx).getSecureSharedPreferences().edit().putString(key, jwt).apply();
    }

    public String getPreferenceKey(String type){
        String key = null;
        switch(type){
            case "AddressCredential":
                key = NoneZKPStorePost;
                break;
            case "CardTokenCredential":
                key = NoneZKPStoreCardTokenKorea;
                break;
            case "IdentificationCredential":
                key = NoneZKPStoreIdCard;
                break;
            case "PhoneCredential":
                key = NoneZKPStoreMobile;
                break;
            case "ProductCredential":
                key = ProductCredential;
                break;
            case "StockServiceCredential":
                key = NoneZKPStoreStock;
                break;
            case "LoginCredential":
                key = NoneZKPStoreLogin;
                break;
            case "delegated_id_VC":
                key = DELEGATOR_VC;
                break;
            case "DelegatedVC":
                key = NoneZKPStoreDelegationToken;
                break;

            case "ProductProofCredential":
                key = ProductProofCredential;
                break;
            default:
                key = NoneZKPStorePost;
                break;
        }
        PrintLog.e("key = " + key);
        return key;
    }

    public String getKor(String type){
        String key = null;

        switch(type){
            case "address":
                key = "주소";
                break;
            case "name":
                key = "이름";
                break;
            case "birth":
            case "birth_date":
                key = "생년월일";
                break;
            case "idcardNum":
                key = "주민등록번호";
                break;
            case "SN":
                key = "일련번호";
                break;
            case "production_date":
                key = "제조일";
                break;
            case "Token":
            case "token":
                key = "토큰";
                break;
            case "phone_num":
                key = "전화번호";
                break;
            case "register_id":
                key = "사용자ID";
                break;
            case "payment":
                key = "결제금액";
                break;
            case "id":
                key = "DID";
                break;
            case "age":
                key = "나이";
                break;
            case "verifier_DID":
                key = "위임자";
                break;
            case "delegated_attr":
                key = "위임내용";
                break;
            case "purpose":
                key = "목적";
                break;
            case "usage":
                key = "사용처";
                break;
            case "verifier":
                key = "인증자";
                break;
            case "type":
                key = "VC type";
                break;
            case "DID_delegator":
                key = "위임자DID";
                break;
            case "ProductProofCredential":
                key = "ProductProofCredential";
                break;
            case "start_date":
                key = "시작일";
                break;
            case "sell_date":
                key = "판매일";
                break;
            case "price":
                key = "가격";
                break;
            case "ProductCredential_id":
                key = "물품정보ID";
                break;
            case "BlockNumber":
                key = "블록넘버";
                break;
            case "buyer_id":
                key = "구매자DID";
                break;
            case "seller_id":
                key = "판매자DID";
                break;
            case "production":
                key = "물품명";
                break;
            case "iit":
                key = "생성날짜";
                break;
            case "company":
                key = "제조사";
                break;
            case "expirationDate":
                key = "만료일";
                break;
            case "issuanceDate":
                key = "발행일";
                break;

            default:
                PrintLog.e("type = " + type);
                key = null;

        }
        PrintLog.e("key = " + key);
        return key;
    }

    public String TxproofMeta(DidDataVo didData, String pruductDID, String buyerDid, BigInteger price, BigInteger blockNumber, String beforeSignedVC, String date){
        Map<String, String> claims = Stream.of(new String[][]{
                {"ProductCredential_id", pruductDID},
                {"seller_id", didData.getDid()},
                {"buyer_id", buyerDid},
                {"BlockNumber", blockNumber.toString()},
                {"price", price.toString()},
                {"sell_date", date}
//                {"sell_date", new SimpleDateFormat("yyyy-MM-dd").format(new Date())}
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        // 이전 거래증명 VC를 claim 에 포함한다.
        if(beforeSignedVC != null){
            claims.put("OldProductProofCredential", beforeSignedVC);
        }
        // 판매자가 VC 서명
        ECKeyPair ecKeyPair = getManagementECKeyPair(didData.getMnemonic());
        MetadiumKey newKey = new MetadiumKey(ecKeyPair);
        MetadiumWallet sellerWallet = new MetadiumWallet(didData.getDid(), newKey);
        VerifiableCredential productVC = new VerifiableCredential();

        productVC.setIssuer(URI.create(sellerWallet.getDid()));
        productVC.addTypes(Collections.singletonList("ProductProofCredential"));
        productVC.setIssuanceDate(new Date());
        productVC.setId(URI.create(UUID.randomUUID().toString()));
        productVC.setCredentialSubject(claims);


        MetadiumSigner signer = new MetadiumSigner(sellerWallet.getDid(), sellerWallet.getKid(), sellerWallet.getKey().getECPrivateKey());
        String signerText = null;
        try{
            signerText = signer.sign(productVC);
        }catch(Exception e){
            PrintLog.e("TxproofMeta error");
        }
        return signerText;
    }
    public String TxproofIcon(DidDataVo didData, String pruductDID, String buyerDid, BigInteger price, BigInteger blockNumber, String beforeSignedVC, String date){
        PrintLog.e("TxproofIcon");
        Map<String, String> claims = Stream.of(new String[][]{
                {"ProductCredential_id", pruductDID},
                {"seller_id", didData.getDid()},
                {"buyer_id", buyerDid},
                {"BlockNumber", blockNumber.toString()},
                {"price", price.toString()},
                {"sell_date", date}
//                {"sell_date", new SimpleDateFormat("yyyy-MM-dd").format(new Date())}
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        // 이전 거래증명 VC를 claim 에 포함한다.
        if(beforeSignedVC != null){
            claims.put("OldProductProofCredential", beforeSignedVC);
        }
        // 판매자가 VC 서명
        VerifiableCredential productVC = new VerifiableCredential();

        productVC.setIssuer(URI.create(didData.getDid()));
        productVC.addTypes(Collections.singletonList("ProductProofCredential"));
        productVC.setIssuanceDate(new Date());
        productVC.setId(URI.create(UUID.randomUUID().toString()));
        productVC.setCredentialSubject(claims);

        AlgorithmProvider.Type type = AlgorithmProvider.Type.ES256K;
        String privateKey = didData.getPrivateKey();
        Algorithm algorithm = AlgorithmProvider.create(type);
        PrivateKey pk = null;
        try{
            pk = algorithm.byteToPrivateKey(Hex.decode(privateKey));
        }catch(KeyPairException e){
            PrintLog.e("TxproofIcon error");
        }
        DidKeyHolder ownerKeyHolder = new DidKeyHolder.Builder()
                .did(didData.getDid())
                .keyId(didData.getIconKeyId())
                .type(type)
                .privateKey(pk)
                .build();
        MetadiumSigner signer = new MetadiumSigner(didData.getDid(), ownerKeyHolder.getKid(), (ECPrivateKey) ownerKeyHolder.getPrivateKey());
        String signerText = null;
        try{
            signerText = signer.sign(productVC);
        }catch(Exception e){
            PrintLog.e("TxproofIcon error");
        }
        return signerText;
    }
    public String vcCreateConfirmMeta(Context context, DidDataVo didData, String hash){
        String vcJwt = null;
        // 만료일
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 2);
        // VC 발급 - Issuer 가 발급
        VerifiableCredential vc = new VerifiableCredential();
        vc.setTypes(Arrays.asList("VerifiableCredential", "StockReportCredential"));
//        vc.setExpirationDate(calendar.getTime());
        vc.setIssuanceDate(new Date());
        Map<String, Object> subject = new HashMap<>();
        subject.put("id", didData.getDid());
        subject.put("hash", hash);
        vc.setCredentialSubject(subject);
        try{
            KeyManager keyManager = new KeyManager(didData.getDid());
            String kid = keyManager.getManagementKeyId(context);
            BigInteger privatekey = keyManager.getPrivate(context);
            ECPrivateKey ecPrivateKey = ECKeyUtils.toECPrivateKey(privatekey, "secp256k1");
            String signedVc = new MetadiumSigner(didData.getDid(), kid, ecPrivateKey).sign(vc);
            vcJwt = signedVc;
            PrintLog.e("VC = " + signedVc);
        }catch(Exception e){
            PrintLog.e("TxproofIcon error");
        }
        return vcJwt;
    }

    public String vcCreateConfirmIcon( DidDataVo didData, String hash){
        String vcJwt = null;
        // 만료일
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 2);
        // VC 발급 - Issuer 가 발급
        VerifiableCredential vc = new VerifiableCredential();
        vc.setTypes(Arrays.asList("VerifiableCredential", "StockReportCredential"));
//        vc.setExpirationDate(calendar.getTime());
        vc.setIssuanceDate(new Date());
        Map<String, Object> subject = new HashMap<>();
        subject.put("id", didData.getDid());
        subject.put("hash", hash);
        vc.setCredentialSubject(subject);

        try{
            AlgorithmProvider.Type type = AlgorithmProvider.Type.ES256K;
            String privateKey = didData.getPrivateKey();
            Algorithm algorithm = AlgorithmProvider.create(type);
            PrivateKey pk = null;
            try{
                pk = algorithm.byteToPrivateKey(Hex.decode(privateKey));
            }catch(KeyPairException e){
                PrintLog.e("vcCreateConfirmIcon error");
            }
            DidKeyHolder ownerKeyHolder = new DidKeyHolder.Builder()
                    .did(didData.getDid())
                    .keyId(didData.getIconKeyId())
                    .type(type)
                    .privateKey(pk)
                    .build();
            String signedVc = new MetadiumSigner(didData.getDid(), ownerKeyHolder.getKid(), (ECPrivateKey) ownerKeyHolder.getPrivateKey()).sign(vc);
            vcJwt = signedVc;
            PrintLog.e("VC = " + signedVc);
        }catch(Exception e){
            PrintLog.e("vcCreateConfirmIcon error");
        }
        return vcJwt;
    }

//    public String vcCreateConfirmIcon(DidDataVo didData, String hash){
//        String requestJwt = null;
//        PrintLog.e("didData = " + didData.getDid());
//        PrintLog.e("private = " + didData.getPrivateKey());
//        AlgorithmProvider.Type type = AlgorithmProvider.Type.ES256K;
//        String privateKey = didData.getPrivateKey();
//        Algorithm algorithm = AlgorithmProvider.create(type);
//        PrivateKey pk = null;
//        try{
//            pk = algorithm.byteToPrivateKey(Hex.decode(privateKey));
//        }catch(KeyPairException e){
//            e.printStackTrace();
//        }
//        DidKeyHolder ownerKeyHolder = new DidKeyHolder.Builder()
//                .did(didData.getDid())
//                .keyId(didData.getIconKeyId())
//                .type(type)
//                .privateKey(pk)
//                .build();
//// Claim type of credential that will be requested
//        Map claims = new HashMap();
//        claims.put("id", didData.getDid());
//        claims.put("hash", hash);
//
//// Generate random nonce to use during request
//        String nonce = Hex.toHexString(AlgorithmProvider.secureRandom().generateSeed(4));
//// Create Credential instance
//        Credential credential = new Credential.Builder()
//                .didKeyHolder(ownerKeyHolder)
//                .nonce(nonce)  // (optional)
//                .build();
//        credential.setTargetDid(didData.getDid());
//        credential.setVersion("1.0");
//        credential.addClaim("id", didData.getDid());
//        credential.addClaim("hash", hash);
//        Date issued = new Date();
//// Default settings
//        long duration = credential.getDuration() * 1000L;  // to milliseconds (for Date class)
//        Date expiration = new Date(issued.getTime() + duration);
//// Issue the signed credential token
//        try{
//            requestJwt = ownerKeyHolder.sign(credential.buildJwt(issued, expiration));
//        }catch(AlgorithmException e){
//            e.printStackTrace();
//        }
//        return requestJwt;
//    }

    private ECKeyPair getManagementECKeyPair(String mnemonic){
        if(mnemonic != null){
            byte[] seed = MnemonicUtils.generateSeed(MnemonicUtils.generateMnemonic(MnemonicUtils.generateEntropy(mnemonic)), (String) null);
            Bip32ECKeyPair master = Bip32ECKeyPair.generateKeyPair(seed);
            return Bip32ECKeyPair.deriveKeyPair(master, BIP44_META_PATH);
        }else{
            return null;
        }
    }
}
