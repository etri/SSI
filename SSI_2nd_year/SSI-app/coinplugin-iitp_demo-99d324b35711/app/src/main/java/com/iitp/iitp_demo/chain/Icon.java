package com.iitp.iitp_demo.chain;

import android.content.Context;

import com.google.gson.reflect.TypeToken;
import com.iitp.iconloop.iitp.securities.SecuritiesReportService;
import com.iitp.iconloop.iitp.securities.exception.IconServiceException;
import com.iitp.iconloop.iitp.securities.icon.IconServiceConfig;
import com.iitp.iitp_demo.FinishListener;
import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.activity.MainActivity;
import com.iitp.iitp_demo.activity.model.BlockChainType;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.api.didResolver.model.ICONDidDocumentVo;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PrintLog;

import org.bouncycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import foundation.icon.did.DidService;
import foundation.icon.did.core.Algorithm;
import foundation.icon.did.core.AlgorithmProvider;
import foundation.icon.did.core.DidKeyHolder;
import foundation.icon.did.core.KeyProvider;
import foundation.icon.did.core.Keystore;
import foundation.icon.did.document.Document;
import foundation.icon.did.document.EncodeType;
import foundation.icon.did.exceptions.AlgorithmException;
import foundation.icon.did.exceptions.KeyPairException;
import foundation.icon.did.exceptions.KeystoreException;
import foundation.icon.did.jwt.Jwt;
import foundation.icon.did.score.ScoreParameter;
import foundation.icon.icx.IconService;
import foundation.icon.icx.KeyWallet;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.data.Bytes;
import foundation.icon.icx.data.IconAmount;
import foundation.icon.icx.transport.http.HttpProvider;

import static com.iitp.iitp_demo.Constants.DID_LIST;

public class Icon{
    private static Icon instance;
    private Algorithm algorithm;
    private String nodeUrl = "http://129.254.194.114:9080/api/v3";
    private String score = "cxbb270900bd1906acef0b9885fb14fa199f41603d";
    private BigInteger networkId = BigInteger.valueOf(15359981);
    private KeyProvider keyProvider;
    private ICONDidDocumentVo didDocument;

    public Icon(){

    }

    public static synchronized Icon getInstance(){
        if(instance == null){
            instance = new Icon();
        }
        return instance;
    }

    /**
     * Create a new KeyProvider using Secp256k algorithm.
     */
    private KeyProvider generateES256K() throws Exception{
        String mnemonic;
//        byte[] initialEntropy = new byte[16];
//        new SecureRandom().nextBytes(initialEntropy);
//        mnemonic = MnemonicUtils.generateMnemonic(initialEntropy);
//        byte[] entropy = MnemonicUtils.generateEntropy(mnemonic);
//        String normalizedMnemonic = MnemonicUtils.generateMnemonic(entropy);
        String normalizedMnemonic = null;
//        PrintLog.e("normalizedMnemonic = " + normalizedMnemonic.toString());
        algorithm = AlgorithmProvider.create(AlgorithmProvider.Type.ES256K);
        normalizedMnemonic = "SSI";
        return algorithm.generateKeyProvider(normalizedMnemonic);
    }

    public void creatIconDid(FinishListener listener){
        IconService iconService = new IconService(new HttpProvider(nodeUrl));
        // DID Document Score Address
        Address scoreAddress = new Address(score);
        PrintLog.e("scoreAddress = " + scoreAddress.toString());
        // Create the DidService object
        DidService didService = new DidService(iconService, networkId, scoreAddress);

        // Create a new KeyProvider
        try{
            keyProvider = generateES256K();
            PrintLog.e("new ES256K KeyProvider : " + keyProvider);
        }catch(Exception e){
            PrintLog.e("creatIconDid error");
        }

        // Important information that user need to have
        String keyId = keyProvider.getKeyId();
        String type = keyProvider.getType().getName();
        String privateKey = Hex.toHexString(algorithm.privateKeyToByte(keyProvider.getPrivateKey()));
        String publicKey = Hex.toHexString(algorithm.publicKeyToByte(keyProvider.getPublicKey()));
        PrintLog.e("keyId = " + keyId);
        PrintLog.e("type = " + type);
        PrintLog.e("privateKey = " + privateKey);
        PrintLog.e("publicKey = " + publicKey);

        // Create a KeyProvider from each String
        // Encoding type used to encode the string of the public key
        EncodeType encodeType = EncodeType.BASE64;
        // Create parameters that will be used when sending the DID registration request
        String param = ScoreParameter.create(keyProvider, encodeType);
        PrintLog.e("param = " + param);
        Document document = null;
        KeyWallet wallet = KeyWallet.load(new Bytes(privateKey));
        BigInteger balance = null;
        try{
            PrintLog.e("wallet.getAddress() = " + wallet.getAddress().toString());
            balance = iconService.getBalance(wallet.getAddress()).execute();
        }catch(IOException e){
            PrintLog.e("creatIconDid error");
        }
        if(balance.compareTo(IconAmount.of("1", IconAmount.Unit.ICX).toLoop()) < 0){
            PrintLog.e("the balance of " + wallet.getAddress() + " < 1 icx\n Use your KeyWallet");
        }
        PrintLog.e("json = " + param);
        try{
            document = didService.create(wallet, param);
        }catch(IOException e){
            PrintLog.e("creatIconDid error");
        }
        // print DID Document
        printDocument(document);
        didDocument = IITPApplication.gson.fromJson(document.toJson(), ICONDidDocumentVo.class);
        listener.finishOK(null);
    }

    private void printDocument(Document document){
        PrintLog.e("DID Document : " + document);
        PrintLog.e("DID Document json: " + document.toJson());
    }

    public void saveDid(Context context, String nickName, BlockChainType type){
        DidDataVo didDataVo;
        ArrayList<DidDataVo> didList;
        String didListJson = CommonPreference.getInstance(context).getSecureSharedPreferences().getString(DID_LIST, null);
        if(didListJson != null){
            didList = IITPApplication.gson.fromJson(didListJson, new TypeToken<ArrayList<DidDataVo>>(){
            }.getType());
        }else{
            didList = new ArrayList<>();
        }
        long time = System.currentTimeMillis();
        String privateKey = Hex.toHexString(algorithm.privateKeyToByte(keyProvider.getPrivateKey()));
        String prbateKye = EncodeType.BASE64.encode(algorithm.privateKeyToByte(keyProvider.getPrivateKey()));
        String publicKey = Hex.toHexString(algorithm.publicKeyToByte(keyProvider.getPublicKey()));
        String did = didDocument.getId();
        String KeyId = keyProvider.getKeyId();

        File file = new File(context.getFilesDir()+"/");
        if(file.exists() && !file.isDirectory()){
            file.mkdirs();
        }
        DidKeyHolder didKeyHolder = new DidKeyHolder.Builder(keyProvider)
                .did(did)
                .build();
        try{
            String  filename = Keystore.storeDidKeyHolder(did, didKeyHolder, file);
            PrintLog.e("file name = "+filename);
        }catch(KeystoreException | IOException e){
            e.printStackTrace();
        }
        PrintLog.e("path = " + file.getAbsolutePath());
        PrintLog.e("KeyId = " + KeyId);
        PrintLog.e("did = " + did);
        PrintLog.e("private Key = " + privateKey);
        PrintLog.e("private Key byte = " + prbateKye);
        PrintLog.e("public Key  = " + publicKey);
        PrintLog.e("time = " + time);
        PrintLog.e("contract = " + type);
        didDataVo = new DidDataVo(did, privateKey, publicKey, time, nickName, type, false, KeyId, "");
        didList.add(didDataVo);
        MainActivity.setDidList(didList, context);
    }

    public void deleteIconDID(Context context,DidDataVo didData, FinishListener listener){
        IconService iconService = new IconService(new HttpProvider(nodeUrl));
        // DID Document Score Address
        Address scoreAddress = new Address(score);
        PrintLog.e("scoreAddress = " + scoreAddress.toString());
        // Create the DidService object
        DidService didService = new DidService(iconService, networkId, scoreAddress);
        String did = didData.getDid();
        AlgorithmProvider.Type type = AlgorithmProvider.Type.ES256K;
        Algorithm algorithm = AlgorithmProvider.create(type);
        DidKeyHolder didKeyHolder = null;
        try{
            String filePath = context.getFilesDir()+"/"+did+".json";
            PrintLog.e("file path = "+filePath);
            didKeyHolder = Keystore.loadDidKeyHolder(did, new File(filePath));
        }catch(IOException | KeystoreException | KeyPairException e){
            e.printStackTrace();
        }
        String privateKey = Hex.toHexString(algorithm.privateKeyToByte(didKeyHolder.getPrivateKey()));
        PrintLog.e("load KeyProvider : " + didKeyHolder);
        KeyWallet wallet = KeyWallet.load(new Bytes(privateKey));
        DidKeyHolder finalDidKeyHolder = didKeyHolder;
        new Thread(() -> {
            try{
                Jwt jwt = ScoreParameter.revokeKey(finalDidKeyHolder, finalDidKeyHolder.getKeyId());
                String signedJwt = null;
                signedJwt = finalDidKeyHolder.sign(jwt);
                PrintLog.e("signedJwt = "+signedJwt);
                Document doc = didService.revokeKey(wallet, signedJwt);
                PrintLog.e("Document = " + doc.toJson().toString());
                listener.finishOK(null);
            }catch(AlgorithmException | IOException e){
                e.printStackTrace();
            }
        }).start();
    }

    public boolean checkHash(String hash){
        String nodeUrl = "http://129.254.194.114:9080";
        BigInteger networkId = BigInteger.valueOf(15359981);
        String scoreAddress = "cx492568e3e014fe3fc7606148069e641e3e0b6a01";
        IconServiceConfig config = new IconServiceConfig.Builder()
                .url(nodeUrl)
                .networkId(networkId)
                .scoreAddress(scoreAddress)
                .build();
        SecuritiesReportService service = new SecuritiesReportService(config);
        boolean result= false;
        try{
            result = service.hasReport(hash);
        }catch(IconServiceException e){
            PrintLog.e("checkHash error");
        }
        return result;
    }
}
