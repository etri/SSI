package com.iitp.iitp_demo.chain.indy;

import android.content.Context;
import android.system.ErrnoException;
import android.system.Os;

import com.google.gson.reflect.TypeToken;
import com.iitp.iitp_demo.FinishListener;
import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.activity.MainActivity;
import com.iitp.iitp_demo.activity.model.BlockChainType;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.activity.model.IndyCredentialVo;
import com.iitp.iitp_demo.api.model.StoreCredentialDataVo;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PreferenceUtil;
import com.iitp.iitp_demo.util.PrintLog;

import org.apache.commons.io.FileUtils;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;
import org.hyperledger.indy.sdk.anoncreds.CredentialsSearchForProofReq;
import org.hyperledger.indy.sdk.blob_storage.BlobStorageReader;
import org.hyperledger.indy.sdk.crypto.Crypto;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidJSONParameters;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.ledger.LedgerResults;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static com.iitp.iitp_demo.Constants.DID_LIST;
import static com.iitp.iitp_demo.chain.indy.PoolUtils.POLLNAME;
import static com.iitp.iitp_demo.chain.indy.PoolUtils.PROTOCOL_VERSION;
import static com.iitp.iitp_demo.chain.indy.PoolUtils.createPoolLedgerConfig;

public class Indy{
    private static Indy instance;
    private static Boolean walletOpened = false;
    private Wallet wallet = null;
    private FinishListener finishListener;
    private Pool pool = null;

    private String WALLET_CREDENTIALS;
    private String wallet_config;

    private String sampleDID = "VsKV7grR1BUE29mG2Fm2kX";

    private String stewardDID = null;
    private String goverDid = null;
    private String mobileDid = null;
    private String trustAnchorDID = null;

    static String indyClientPath;

    private String myDID = null;

    private static String WALLET_KEY = "key";
    private String WALLET_NAME = "WalletSSI";
    private DidResults.CreateAndStoreMyDidResult myDidResult;
    private static String credReqMetadataJson = null;
    private static String credentialOffer = null;
    private static String credDefJson = null;
    private static String schemaJson = null;
    private static String credDefId = null;
    private static String schemaId = null;
    private Context ctx;

    public Indy(Context context){
        File dataDir = context.getApplicationContext().getDataDir();
        PrintLog.e("datadir=" + dataDir.getAbsolutePath());
        File externalFilesDir = context.getExternalFilesDir(null);
        String path = externalFilesDir.getAbsolutePath();
        PrintLog.e("externalFilesDir=" + path);
        indyClientPath = path + "/" + ".indy_client";
        ctx = context;
        try{
            Os.setenv("EXTERNAL_STORAGE", path, true);
            PrintLog.e("indyClientPath = " + indyClientPath);
            File file = new
                    File(indyClientPath);
            if(!file.exists()){
                file.mkdir();
            }
            LibIndy.init();
//            LibIndy.api.indy_set_log_max_lvl(5);

        }catch(ErrnoException e){
            PrintLog.e("Indy getInstace Error");
        }

        // 파일 삭제
        File[] files = externalFilesDir.listFiles();
        for(int i = 0; i < files.length; ++i){
            File file = files[i];
            PrintLog.e("file path =  " + file.getAbsolutePath());
            if(file.isDirectory()){
                PrintLog.e("directory:" + file.getName());
                if(".indy_client".equals(file.getName())){
                    String[] children = file.list();
                    for(String child : children){
                        PrintLog.e("delete deleting:" + child);
                        new File(file, child).delete();
                    }
                }
            }else{
                PrintLog.e("log file     :" + file.getName());
            }
        }

        WalletCredentailVo walletCredentailVo = new WalletCredentailVo(WALLET_KEY);
        WALLET_CREDENTIALS = IITPApplication.gson.toJson(walletCredentailVo);

        String TYPE = "default";
        WalletConfigVo walletConfigVo = new WalletConfigVo(WALLET_NAME, TYPE);
        wallet_config = IITPApplication.gson.toJson(walletConfigVo);

    }

    public String getCredentialsWorksForEmptyFilter() throws IndyException{
        String credentials = null;
        if(wallet == null){
            createWallet();
            JSONObject json = new JSONObject();
            String filter = json.toString();
            try{
                credentials = Anoncreds.proverGetCredentials(wallet, filter).get();
            }catch(ExecutionException | InterruptedException e){
                PrintLog.e("getCredentialsWorksForEmptyFilter error");
            }
        }else{

            JSONObject json = new JSONObject();
            String filter = json.toString();
            try{
                credentials = Anoncreds.proverGetCredentials(wallet, filter).get();
            }catch(ExecutionException | InterruptedException e){
                PrintLog.e("getCredentialsWorksForEmptyFilter error");
            }
        }
        return credentials;
    }

    public static synchronized Indy getInstance(Context ctx){
        if(instance == null){
            instance = new Indy(ctx);
        }
        return instance;
    }

    /**
     * indy did 생성
     *
     * @param finishListener listener
     */
    public void createIndyDid(FinishListener finishListener){
        this.finishListener = finishListener;
        createPoolWorksForConfigJSON();
        try{
            closeWallet();
        }catch(Exception e){
            PrintLog.e("createIndyDid Error");
        }
        createWallet();
        createStewardDID();
        createTrustDID();
        createDid();
        String did = myDidResult.getDid();
        PrintLog.e("did = " + did);
        finishListener.finishOK(did);
    }

    public void setRequestIDCredentialData(Context ctx){
        PrintLog.e("setRequestIDCredentialData");
        PrintLog.e("credDefId = " + credDefId);
        PrintLog.e("schemaId = " + schemaId);
        PrintLog.e("credDefId = " + credReqMetadataJson);
        PrintLog.e("credentialOffer = " + credentialOffer);
        StoreCredentialDataVo storeCredentialDataV = new StoreCredentialDataVo(credReqMetadataJson, credentialOffer, credDefJson, schemaJson, credDefId, schemaId);
        String saveJson = IITPApplication.gson.toJson(storeCredentialDataV);
        CommonPreference.getInstance(ctx).getSecureSharedPreferences().edit().putString("idCredentialRequest", saveJson).apply();
    }

    public StoreCredentialDataVo getRequestIDCredentialData(Context ctx){
        PrintLog.e("getRequestIDCredentialData");
        String saveJson = CommonPreference.getInstance(ctx).getSecureSharedPreferences().getString("idCredentialRequest", null);
        PrintLog.e("getRequestIDCredentialData : " + saveJson);
        StoreCredentialDataVo storeCredentialData = IITPApplication.gson.fromJson(saveJson, StoreCredentialDataVo.class);
        if(storeCredentialData != null){
            PrintLog.e("getRequestIDCredentialData = " + storeCredentialData.getSchemaId());
        }
        return storeCredentialData;
    }

    public void setRequestUniCredentialData(Context ctx){
        PrintLog.e("setRequestUniCredentialData");
        PrintLog.e("credDefId = " + credDefId);
        PrintLog.e("schemaId = " + schemaId);

        StoreCredentialDataVo storeCredentialDataV = new StoreCredentialDataVo(credReqMetadataJson, credentialOffer, credDefJson, schemaJson, credDefId, schemaId);
        String saveJson = IITPApplication.gson.toJson(storeCredentialDataV);
        CommonPreference.getInstance(ctx).getSecureSharedPreferences().edit().putString("UniCredentialRequest", saveJson).apply();
    }

    public void setRequestJobCredentialData(Context ctx){
        PrintLog.e("setRequestJobCredentialData");
        PrintLog.e("credDefId = " + credDefId);
        PrintLog.e("schemaId = " + schemaId);

        StoreCredentialDataVo storeCredentialDataV = new StoreCredentialDataVo(credReqMetadataJson, credentialOffer, credDefJson, schemaJson, credDefId, schemaId);
        String saveJson = IITPApplication.gson.toJson(storeCredentialDataV);
        CommonPreference.getInstance(ctx).getSecureSharedPreferences().edit().putString("JobCredentialRequest", saveJson).apply();
    }

    public StoreCredentialDataVo getRequestUniCredentialData(Context ctx){
        PrintLog.e("getRequestUniCredentialData");
        String saveJson = CommonPreference.getInstance(ctx).getSecureSharedPreferences().getString("UniCredentialRequest", null);
        PrintLog.e("getRequestUniCredentialData : " + saveJson);
        StoreCredentialDataVo storeCredentialData = IITPApplication.gson.fromJson(saveJson, StoreCredentialDataVo.class);
        if(storeCredentialData != null){
            PrintLog.e("getRequestUniCredentialData = " + storeCredentialData.getSchemaId());
        }
        return storeCredentialData;
    }

    public StoreCredentialDataVo getRequestJobCredentialData(Context ctx){
        PrintLog.e("getRequestJobCredentialData");
        String saveJson = CommonPreference.getInstance(ctx).getSecureSharedPreferences().getString("JobCredentialRequest", null);
        PrintLog.e("saveJson : " + saveJson);
        StoreCredentialDataVo storeCredentialData = IITPApplication.gson.fromJson(saveJson, StoreCredentialDataVo.class);
        if(storeCredentialData != null){
            PrintLog.e("getRequestJobCredentialData = " + storeCredentialData.getSchemaId());
        }
        return storeCredentialData;
    }

    public String storeCredential(String storeid, String credential){
        createWallet();
        PrintLog.e("storeid = " + storeid);
        PrintLog.e("credential = " + credential);
        PrintLog.e("credDefJson = " + credDefJson);
        PrintLog.e("credReqMetadataJson = " + credReqMetadataJson);
        PrintLog.e("credDefId = " + credDefId);
        String proverStoreCredentialJson = null;
        try{
            proverStoreCredentialJson = Anoncreds.proverStoreCredential(wallet, storeid,
                    credReqMetadataJson, credential, credDefJson, null).get();
        }catch(ExecutionException | InterruptedException | IndyException e){
            PrintLog.e("storeCredential error");
        }
        PrintLog.e("proverStoreCredentialJson = " + proverStoreCredentialJson);
        return proverStoreCredentialJson;
    }

    public String storeJobCredential(String storeid, String credential, String did){
        createWallet();
        PrintLog.e("storeid = " + storeid);
        PrintLog.e("credential = " + credential);
        PrintLog.e("credDefJson = " + credDefJson);
        PrintLog.e("credReqMetadataJson = " + credReqMetadataJson);
        PrintLog.e("credDefId = " + credDefId);
        String proverStoreCredentialJson = null;
        String getRevRegDefReq = null;
        String getRevRegDefResponse = null;
        LedgerResults.ParseResponseResult revRegInfo1 = null;
        String revocRegDefJson = null;
        try{
            getRevRegDefReq = Ledger.buildGetRevocRegDefRequest(did, "ES52TT3zeLLvukKTjX8fMC:4:ES52TT3zeLLvukKTjX8fMC:3:CL:446:company_TAG_CD:CL_ACCUM:company_TAG_CD").get();
            PrintLog.e("getRevRegDefReq = " + getRevRegDefReq);
            getRevRegDefResponse = Ledger.submitRequest(pool, getRevRegDefReq).get();
            PrintLog.e("getRevRegDefResponse = " + getRevRegDefResponse);
            revRegInfo1 = Ledger.parseGetRevocRegDefResponse(getRevRegDefResponse).get();
            PrintLog.e("revRegInfo1 = " + revRegInfo1.toString());
            revocRegDefJson = revRegInfo1.getObjectJson();
            PrintLog.e("revocRegDefJson = " + getRevRegDefResponse);
        }catch(ExecutionException | InterruptedException | IndyException e){
            PrintLog.e("storeJobCredential error");
        }

        try{
            proverStoreCredentialJson = Anoncreds.proverStoreCredential(wallet, storeid,
                    credReqMetadataJson, credential, credDefJson, revocRegDefJson).get();
        }catch(ExecutionException | InterruptedException | IndyException e){
            PrintLog.e("storeJobCredential error");
        }
        PrintLog.e("proverStoreCredentialJson = " + proverStoreCredentialJson);
        return proverStoreCredentialJson;
    }

    public void deleteCredential(String storeId){
        createWallet();
        PrintLog.e("storeid = " + storeId);
        try{
            Anoncreds.proverDeleteCredential(wallet, storeId).get();
        }catch(IndyException | InterruptedException | ExecutionException e){
            PrintLog.e("deleteCredential error");
        }finally{
            PrintLog.e("deleteCredential");
        }
    }

    public String createCredentialRequest(String did, String credDefId1, String offer, String shemaId){
        PrintLog.e("did = " + did);
        PrintLog.e("credDefId1 = " + credDefId1);
        PrintLog.e("offer = " + offer);
        PrintLog.e("shemaId = " + shemaId);
        credentialOffer = offer;
        schemaId = shemaId;
        credDefId = credDefId1;
        // Prover create Master Secret
        String materSecret = null;
        try{
            materSecret = Anoncreds.proverCreateMasterSecret(wallet, did).get();
            PrintLog.e("materSecret = " + materSecret);
        }catch(ExecutionException | InterruptedException | IndyException e){
            PrintLog.e("error " + e.getMessage());
            if(e.getMessage().indexOf("Another master-secret with the specified") >= 0){
                // ignore
            }else{
                PrintLog.e("error " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

        //get schemaJaon
        String getSchemaRequest = null;
        String getSchemaResponse = null;
        try{
            PrintLog.e("did = " + did);
            PrintLog.e("shemaId = " + shemaId);
            getSchemaRequest = Ledger.buildGetSchemaRequest(did, shemaId).get();
            PrintLog.e("shemaId = " + shemaId);
            getSchemaResponse = Ledger.submitRequest(pool, getSchemaRequest).get();
            PrintLog.e("getSchemaResponse = " + getSchemaResponse);
            LedgerResults.ParseResponseResult schemaInfo2 = Ledger.parseGetSchemaResponse(getSchemaResponse).get();
            String schemaId = schemaInfo2.getId();
            PrintLog.e("schemaId = " + schemaId);
            schemaJson = schemaInfo2.getObjectJson();
            PrintLog.e("schemaJso = " + schemaJson);
        }catch(ExecutionException | InterruptedException | IndyException e){
            PrintLog.e("createCredentialRequest error");
        }

        //get credential def
        String credDef = null;
        LedgerResults.ParseResponseResult credDefIdInfo = null;
        try{
            credDef = Ledger.buildGetCredDefRequest(did, credDefId).get();
            PrintLog.e("credDef = " + credDef);
            String getCredDefResponse = Ledger.submitRequest(pool, credDef).get();
            PrintLog.e("getCredDefResponse = " + getCredDefResponse);
            credDefIdInfo = Ledger.parseGetCredDefResponse(getCredDefResponse).get();
            String credDefIdString = credDefIdInfo.getId();
            credDefJson = credDefIdInfo.getObjectJson();
            PrintLog.e("credDefIdString = " + credDefIdString);
            PrintLog.e("credDefJson = " + credDefJson);
        }catch(ExecutionException | InterruptedException | IndyException e){
            PrintLog.e("createCredentialRequest error");
        }
        // Prover create CredentialReq
        String credReqJson = null;

        AnoncredsResults.ProverCreateCredentialRequestResult createCredReqResult;
        try{
            PrintLog.e("-----------------------------------");
            PrintLog.e("did = " + did);
//            PrintLog.e("credDefJson = " + credDefJson);
//            PrintLog.e("offer = " + offer);
            PrintLog.e("materSecret = " + materSecret);
            if(materSecret == null){
                materSecret = did;
            }
            PrintLog.e("-----------------------------------");
            createCredReqResult = Anoncreds.proverCreateCredentialReq(wallet, did, offer, credDefJson, materSecret).get();
            credReqJson = createCredReqResult.getCredentialRequestJson();
            credReqMetadataJson = createCredReqResult.getCredentialRequestMetadataJson();
            PrintLog.e("credReqJson = " + credReqJson);
            PrintLog.e("credReqMetadataJson = " + credReqMetadataJson);
        }catch(ExecutionException | InterruptedException | IndyException e){
            PrintLog.e("createCredentialRequest error");
        }
        return credReqJson;
    }

    public String createProofRequest(String proofRequest, String did, String type){
        PrintLog.e("createProofRequest");
        // Issuer create Credential
        PrintLog.e("proofRequest = " + proofRequest);
        PrintLog.e("did = " + did);
        createPoolWorksForConfigJSON();
        createWallet();
//        proofRequest = "{\"name\":\"transcript_issuance\",\"nonce\":\"897300791344996745982544\",\"requested_attributes\":{\"attr2_referent\":{\"name\":\"address\",\"restrictions\":[{\"cred_def_id\":\"8nCWZ4JRYajbepaTWF7VUS:3:CL:261:government_TAG_CD\"}]},\"attr1_referent\":{\"name\":\"name\",\"restrictions\":[{\"cred_def_id\":\"8nCWZ4JRYajbepaTWF7VUS:3:CL:261:government_TAG_CD\"}]}},\"version\":\"0.1\"}";
        // Prover get Credential
        String credentialsForProofJson = null;
        String proverCreateProofreturn = null;
        try{
            PrintLog.e("proofRequestJson = " + proofRequest);
            CredentialsSearchForProofReq credentialsSearch = CredentialsSearchForProofReq.open(wallet, proofRequest, null).get();
//            PrintLog.e("credentialsSearch = " + credentialsSearch);
            credentialsForProofJson = Anoncreds.proverGetCredentialsForProofReq(wallet, proofRequest).get();
            PrintLog.e("credentialsForProofJson = " + credentialsForProofJson);

            JSONObject credentialsForProof = null;
            String credentialIdForAttribute1 = null;
            String credentialIdForAttribute2 = null;
            try{
                int index = 0;
//                credentialsForProof = new JSONObject(credentialsForProofJson);
                JSONArray credentialsForAttribute1 = new JSONArray(credentialsSearch.fetchNextCredentials("attr1_referent", 100).get());
                int size = credentialsForAttribute1.length();
                PrintLog.e("credentialsForAttribute1 size = " + size);
                for(int i = 0; i < size; i++){
                    String dataDID = credentialsForAttribute1.getJSONObject(i).getJSONObject("cred_info").getJSONObject("attrs").getString("id");
                    PrintLog.e("dataDID = " + dataDID);
                    PrintLog.e("did = " + did);
                    if(did.equals(dataDID)){
                        index = i;
                        break;
                    }
                }
                credentialIdForAttribute1 = credentialsForAttribute1.getJSONObject(index).getJSONObject("cred_info").getString("referent");
                JSONArray credentialsForAttribute2 = new JSONArray(credentialsSearch.fetchNextCredentials("attr2_referent", 100).get());
                credentialIdForAttribute2 = credentialsForAttribute2.getJSONObject(index).getJSONObject("cred_info").getString("referent");
                PrintLog.e("credentialIdForAttribute1 = " + credentialIdForAttribute1);
                PrintLog.e("credentialIdForAttribute2 = " + credentialIdForAttribute2);
                try{
                    credentialsSearch.close();
                }catch(Exception e){
                    PrintLog.e("createProofRequest Error");
                }
            }catch(JSONException e){
                PrintLog.e("createProofRequest Error");
            }

            String requestedCredentialsJson = null;
            try{
                requestedCredentialsJson = new JSONObject()
                        .put("self_attested_attributes", new JSONObject())
                        .put("requested_attributes", new JSONObject()
                                .put("attr1_referent", new JSONObject()
                                        .put("cred_id", credentialIdForAttribute1)
                                        .put("revealed", true)
                                )
                                .put("attr2_referent", new JSONObject()
                                        .put("cred_id", credentialIdForAttribute2)
                                        .put("revealed", true)
                                )
                        )
                        .put("requested_predicates", new JSONObject())
                        .toString();
            }catch(JSONException e){
                PrintLog.e("createProofRequest Error");
            }

            try{
                PrintLog.e("proofRequest = " + proofRequest);
                PrintLog.e("requestedCredentialsJson = " + requestedCredentialsJson);
                PrintLog.e("did = " + did);
                PrintLog.e("schemaJson = " + schemaJson);
//                PrintLog.e("credDefJson = " + credDefJson);
                String credentialDefsJson = null;
                String schemasJson = null;
                String revocStates = new JSONObject().toString();
//                if(schemaJson == null || credDefJson == null){
                StoreCredentialDataVo data;
                if(type.equals("id")){
                    data = getRequestIDCredentialData(ctx);
                }else if(type.equals("uni")){
                    data = getRequestUniCredentialData(ctx);
                }else{
                    data = getRequestIDCredentialData(ctx);
                }
                if(data != null){
                    schemaJson = data.getSchemaJson();
                    credDefJson = data.getCredDefJson();
                    credDefId = data.getCredentialDefId();
                    schemaId = data.getSchemaId();
                }
                PrintLog.e("schemaJson = " + schemaJson);
//                    PrintLog.e("credDefJson = "+credDefJson);
//                }
                try{
                    schemasJson = new JSONObject().put(schemaId, new JSONObject(schemaJson)).toString();
                    credentialDefsJson = new JSONObject().put(credDefId, new JSONObject(credDefJson)).toString();
//                    PrintLog.e("credentialDefsJson = " + credentialDefsJson);
                    PrintLog.e("schemasJson = " + schemasJson);
                }catch(JSONException e){
                    PrintLog.e("createProofRequest Error");
                }
                proverCreateProofreturn = Anoncreds.proverCreateProof(wallet, proofRequest, requestedCredentialsJson, did, schemasJson, credentialDefsJson, revocStates).get();
            }catch(ExecutionException | InterruptedException | IndyException e){
                PrintLog.e("createProofRequest error");
            }
            PrintLog.e("proverCreateProofreturn = " + proverCreateProofreturn);
//            PrintLog.e("credentialsForProofJson = " + credentialsForProofJson);
        }catch(IndyException | InterruptedException | ExecutionException e){
            PrintLog.e("createProofRequest Error");
            try{
                closeWallet();
            }catch(Exception ex){
                PrintLog.e("createProofRequest Error");
            }
        }
        return proverCreateProofreturn;

    }

    public String createProofUniRequest(String proofRequest, String did){
        PrintLog.e("createProofUniRequest");
        // Issuer create Credential
        PrintLog.e("proofRequest = " + proofRequest);
        PrintLog.e("did = " + did);
        createPoolWorksForConfigJSON();
        createWallet();
        // Prover get Credential
        String credentialsForProofJson = null;
        String proverCreateProofreturn = null;
        try{
//            proofRequest = "{\"name\":\"job_apply_issuance\",\"nonce\":\"368463605289502385020903\",\"requested_attributes\":{\"attr2_referent\":{\"name\":\"birthdate\",\"restrictions\":[{\"cred_def_id\":\"MRVL4VmiiXbDWyLkWdQVRn:3:CL:264:university_TAG_CD\"}]},\"attr1_referent\":{\"name\":\"name\",\"restrictions\":[{\"cred_def_id\":\"MRVL4VmiiXbDWyLkWdQVRn:3:CL:264:university_TAG_CD\"}]}},\"requested_predicates\":{\"predicate1_referent\":{\"name\":\"gpa\",\"p_type\":\">=\",\"p_value\":3}},\"version\":\"0.1\"}";
//            proofRequest = "{\"name\":\"job_apply_issuance\",\"nonce\":\"055557340203837836626263\",\"requested_attributes\":{\"attr2_referent\":{\"name\":\"birthdate\",\"restrictions\":[{\"cred_def_id\":\"MRVL4VmiiXbDWyLkWdQVRn:3:CL:264:university_TAG_CD\"}]},\"attr1_referent\":{\"name\":\"name\",\"restrictions\":[{\"cred_def_id\":\"MRVL4VmiiXbDWyLkWdQVRn:3:CL:264:university_TAG_CD\"}]}},\"version\":\"0.1\"}";
            PrintLog.e("proofRequestJson = " + proofRequest);
            CredentialsSearchForProofReq credentialsSearch = CredentialsSearchForProofReq.open(wallet, proofRequest, null).get();
//            PrintLog.e("credentialsSearch = " + credentialsSearch);
            credentialsForProofJson = Anoncreds.proverGetCredentialsForProofReq(wallet, proofRequest).get();
            PrintLog.e("credentialsForProofJson = " + credentialsForProofJson);

            JSONObject credentialsForProof = null;
            String credentialIdForAttribute1 = null;
            String credentialIdForAttribute2 = null;
            String credentialIdForAttribute3 = null;
            String credentialIdForAttribute4 = null;
            String credentialIdForPredicate = null;
            try{
                int index = 0;
//                credentialsForProof = new JSONObject(credentialsForProofJson);
                JSONArray credentialsForAttribute1 = new JSONArray(credentialsSearch.fetchNextCredentials("attr1_referent", 100).get());
                int size = credentialsForAttribute1.length();
                PrintLog.e("credentialsForAttribute1 size = " + size);
                for(int i = 0; i < size; i++){
                    String dataDID = credentialsForAttribute1.getJSONObject(i).getJSONObject("cred_info").getJSONObject("attrs").getString("id");
                    PrintLog.e("dataDID = " + dataDID);
                    PrintLog.e("did = " + did);
                    if(did.equals(dataDID)){
                        index = i;
                        break;
                    }
                }
                credentialIdForAttribute1 = credentialsForAttribute1.getJSONObject(index).getJSONObject("cred_info").getString("referent");
                JSONArray credentialsForAttribute2 = new JSONArray(credentialsSearch.fetchNextCredentials("attr2_referent", 100).get());
                credentialIdForAttribute2 = credentialsForAttribute2.getJSONObject(index).getJSONObject("cred_info").getString("referent");
                JSONArray credentialsForAttribute3 = new JSONArray(credentialsSearch.fetchNextCredentials("attr3_referent", 100).get());
                credentialIdForAttribute3 = credentialsForAttribute3.getJSONObject(index).getJSONObject("cred_info").getString("referent");
                JSONArray credentialsForAttribute4 = new JSONArray(credentialsSearch.fetchNextCredentials("attr4_referent", 100).get());
                credentialIdForAttribute4 = credentialsForAttribute4.getJSONObject(index).getJSONObject("cred_info").getString("referent");
                JSONArray credentialsForPredicate = new JSONArray(credentialsSearch.fetchNextCredentials("predicate1_referent", 100).get());
                credentialIdForPredicate = credentialsForPredicate.getJSONObject(index).getJSONObject("cred_info").getString("referent");
                PrintLog.e("credentialIdForAttribute1 = " + credentialIdForAttribute1);
                PrintLog.e("credentialIdForAttribute2 = " + credentialIdForAttribute2);
                PrintLog.e("credentialIdForAttribute3 = " + credentialIdForAttribute3);
                PrintLog.e("credentialIdForAttribute4 = " + credentialIdForAttribute4);
                PrintLog.e("credentialIdForPredicate = " + credentialIdForPredicate);
                try{
                    credentialsSearch.close();
                }catch(Exception e){
                    PrintLog.e("createProofUniRequest Error");
                }
            }catch(JSONException e){
                PrintLog.e("createProofUniRequest Error");
            }

            String requestedCredentialsJson = null;
            try{
                requestedCredentialsJson = new JSONObject()
                        .put("self_attested_attributes", new JSONObject())
                        .put("requested_attributes", new JSONObject()
                                .put("attr1_referent", new JSONObject()
                                        .put("cred_id", credentialIdForAttribute1)
                                        .put("revealed", true)
                                )
                                .put("attr2_referent", new JSONObject()
                                        .put("cred_id", credentialIdForAttribute2)
                                        .put("revealed", true)
                                ).put("attr3_referent", new JSONObject()
                                        .put("cred_id", credentialIdForAttribute3)
                                        .put("revealed", true)
                                ).put("attr4_referent", new JSONObject()
                                        .put("cred_id", credentialIdForAttribute4)
                                        .put("revealed", true)
                                )
                        )
                        .put("requested_predicates", new JSONObject()
                                .put("predicate1_referent", new JSONObject()
                                        .put("cred_id", credentialIdForPredicate)
                                )
                        )
                        .toString();
            }catch(JSONException e){
                PrintLog.e("createProofUniRequest Error");
            }

            try{
                PrintLog.e("proofRequest = " + proofRequest);
                PrintLog.e("requestedCredentialsJson = " + requestedCredentialsJson);
                PrintLog.e("did = " + did);
                PrintLog.e("schemaJson = " + schemaJson);
//                PrintLog.e("credDefJson = " + credDefJson);
                String credentialDefsJson = null;
                String schemasJson = null;
                String revocStates = new JSONObject().toString();
//                if(schemaJson == null || credDefJson == null){
                StoreCredentialDataVo data;
                data = getRequestUniCredentialData(ctx);
                if(data != null){
                    schemaJson = data.getSchemaJson();
                    credDefJson = data.getCredDefJson();
                    credDefId = data.getCredentialDefId();
                    schemaId = data.getSchemaId();
                }
                PrintLog.e("schemaJson = " + schemaJson);
                try{
                    schemasJson = new JSONObject().put(schemaId, new JSONObject(schemaJson)).toString();
                    credentialDefsJson = new JSONObject().put(credDefId, new JSONObject(credDefJson)).toString();
                    PrintLog.e("schemasJson = " + schemasJson);
                }catch(JSONException e){
                    PrintLog.e("createProofUniRequest Error");
                }
                proverCreateProofreturn = Anoncreds.proverCreateProof(wallet, proofRequest, requestedCredentialsJson, did, schemasJson, credentialDefsJson, revocStates).get();
            }catch(ExecutionException | InterruptedException | IndyException e){
                PrintLog.e("createProofUniRequest Error");
            }
            PrintLog.e("proverCreateProofreturn = " + proverCreateProofreturn);
//            PrintLog.e("credentialsForProofJson = " + credentialsForProofJson);
        }catch(IndyException | InterruptedException | ExecutionException e){
            PrintLog.e("createProofUniRequest error");
            try{
                closeWallet();
            }catch(Exception ex){
                PrintLog.e("createProofUniRequest Error");
            }
        }
        return proverCreateProofreturn;

    }

    public String createProofJobRequest(String proofRequest, String did){
        String rev_reg_id = null;
        String cred_rev_id = null;
        String schema_id = null;
        String cred_def_id = null;
        PrintLog.e("createProofJobRequest");
        // Issuer create Credential
        PrintLog.e("proofRequest = " + proofRequest);
        PrintLog.e("did = " + did);
        createPoolWorksForConfigJSON();
        createWallet();
        long to = System.currentTimeMillis() / 1000;
        // Prover get Credential
        String credentialsForProofJson = null;
        String proverCreateProofreturn = null;
        try{
            String storeData = getCredentialsWorksForEmptyFilter();
            ArrayList<IndyCredentialVo> indyCredentialVos = IITPApplication.gson.fromJson(storeData, new TypeToken<ArrayList<IndyCredentialVo>>(){
            }.getType());
            for(IndyCredentialVo temp : indyCredentialVos){
                if(temp.getSchema_id().contains("company")){
                    rev_reg_id = temp.getRev_reg_id();
                    cred_rev_id = temp.getCred_rev_id();
                    schema_id = temp.getSchema_id();
                    cred_def_id = temp.getCred_def_id();
                }
            }
//            proofRequest = "{\"name\":\"job_apply_issuance\",\"nonce\":\"368463605289502385020903\",\"requested_attributes\":{\"attr2_referent\":{\"name\":\"birthdate\",\"restrictions\":[{\"cred_def_id\":\"MRVL4VmiiXbDWyLkWdQVRn:3:CL:264:university_TAG_CD\"}]},\"attr1_referent\":{\"name\":\"name\",\"restrictions\":[{\"cred_def_id\":\"MRVL4VmiiXbDWyLkWdQVRn:3:CL:264:university_TAG_CD\"}]}},\"requested_predicates\":{\"predicate1_referent\":{\"name\":\"gpa\",\"p_type\":\">=\",\"p_value\":3}},\"version\":\"0.1\"}";
//            proofRequest = "{\"name\":\"job_apply_issuance\",\"nonce\":\"055557340203837836626263\",\"requested_attributes\":{\"attr2_referent\":{\"name\":\"birthdate\",\"restrictions\":[{\"cred_def_id\":\"MRVL4VmiiXbDWyLkWdQVRn:3:CL:264:university_TAG_CD\"}]},\"attr1_referent\":{\"name\":\"name\",\"restrictions\":[{\"cred_def_id\":\"MRVL4VmiiXbDWyLkWdQVRn:3:CL:264:university_TAG_CD\"}]}},\"version\":\"0.1\"}";
            PrintLog.e("proofRequestJson = " + proofRequest);
            CredentialsSearchForProofReq credentialsSearch = CredentialsSearchForProofReq.open(wallet, proofRequest, null).get();
            PrintLog.e("credentialsSearch = " + credentialsSearch);
            credentialsForProofJson = Anoncreds.proverGetCredentialsForProofReq(wallet, proofRequest).get();
            PrintLog.e("credentialsForProofJson = " + credentialsForProofJson);

            JSONObject credentialsForProof = null;
            String credentialIdForAttribute1 = null;
            try{
                int index = 0;
//                credentialsForProof = new JSONObject(credentialsForProofJson);
                JSONArray credentialsForAttribute1 = new JSONArray(credentialsSearch.fetchNextCredentials("attr1_referent", 100).get());
                int size = credentialsForAttribute1.length();
                PrintLog.e("credentialsForAttribute1 size = " + size);
                for(int i = 0; i < size; i++){
                    String dataDID = credentialsForAttribute1.getJSONObject(i).getJSONObject("cred_info").getJSONObject("attrs").getString("id");
                    PrintLog.e("dataDID = " + dataDID);
                    PrintLog.e("did = " + did);
                    if(did.equals(dataDID)){
                        index = i;
                        break;
                    }
                }
                PrintLog.e("index = " + index);
                credentialIdForAttribute1 = credentialsForAttribute1.getJSONObject(index).getJSONObject("cred_info").getString("referent");
                PrintLog.e("credentialIdForAttribute1 = " + credentialIdForAttribute1);
                try{
                    credentialsSearch.close();
                }catch(Exception e){
                    PrintLog.e("createProofJobRequest Error");
                }
            }catch(JSONException e){
                PrintLog.e("createProofJobRequest Error");
            }

            String requestedCredentialsJson = null;
            try{
                requestedCredentialsJson = new JSONObject()
                        .put("self_attested_attributes", new JSONObject())
                        .put("requested_attributes", new JSONObject()
                                .put("attr1_referent", new JSONObject()
                                        .put("cred_id", credentialIdForAttribute1)
                                        .put("revealed", true)
                                )
                        ).put("requested_predicates", new JSONObject())
                        .toString();
            }catch(JSONException e){
                PrintLog.e("createProofJobRequest Error");
            }

            String getRevRegDefReq = Ledger.buildGetRevocRegDefRequest(null, rev_reg_id).get();
//            PrintLog.e("getRevRegDefReq = "+getRevRegDefReq);
            String getRevRegDefResponse = Ledger.submitRequest(pool, getRevRegDefReq).get();
//            PrintLog.e("getRevRegDefResponse = "+getRevRegDefResponse);
            LedgerResults.ParseResponseResult revRegInfo1 = Ledger.parseGetRevocRegDefResponse(getRevRegDefResponse).get();
            PrintLog.e("revRegInfo1 = " + revRegInfo1.toString());
            String revocRegDefJson = revRegInfo1.getObjectJson();
            PrintLog.e("revocRegDefJson = " + getRevRegDefResponse);
            // Verifier gets RevocationRegistry from Ledger
            String getRevRegReq = Ledger.buildGetRevocRegRequest(did, rev_reg_id, to).get();
            PrintLog.e("getRevRegReq = " + getRevRegReq);
            String getRevRegResp = Ledger.submitRequest(pool, getRevRegReq).get();
            PrintLog.e("getRevRegResp = " + getRevRegResp);
//            long from = to;
//            to = System.currentTimeMillis() / 1000;
            long from = 0;
            to = System.currentTimeMillis() / 1000;
            // Prover gets RevocationRegistryDelta from Ledger
            String getRevRegDeltaRequest = Ledger.buildGetRevocRegDeltaRequest(null, rev_reg_id, (int) from, (int) to).get();
//            PrintLog.e("getRevRegDeltaRequest = "+getRevRegDeltaRequest);
            String getRevRegDeltaResponse = Ledger.submitRequest(pool, getRevRegDeltaRequest).get();
//            PrintLog.e("getRevRegDeltaResponse = "+getRevRegDeltaResponse);
            LedgerResults.ParseRegistryResponseResult revRegInfo2 = Ledger.parseGetRevocRegDeltaResponse(getRevRegDeltaResponse).get();

            PrintLog.e("RevRegDelta = " + revRegInfo2.toString());
            String revocRegDeltaJson = revRegInfo2.getObjectJson();
            long timestamp = revRegInfo2.getTimestamp();
//            String tailsWriterConfig = new JSONObject(String.format("{\"base_dir\":\"%s\", \"uri_pattern\":\"\"}",
//                    EnvironmentUtils.getTmpPath("tails").replace('\\', '/'))).toString();

//            String tailsWriterConfig = new JSONObject(String.format("{\"base_dir\":\"%s\"}",
//                    EnvironmentUtils.getTmpPath("tails").replace('\\', '/'))).toString();
            String path = EnvironmentUtils.getTmpPath("tails.txt");
            String tailsWriterConfig = new JSONObject(String.format("{\"base_dir\":\"%s\",\"uri_pattern\":\"\"}", path)).toString();
//            tailsWriterConfig = "{\"base_dir\":\"/data/user/0/com.iitp.iitp_demo/cache/indy/tails\",\"uri_pattern\":\"\" }";
            PrintLog.e("tailsWriterConfig = " + tailsWriterConfig);
//            File file = new File(path);
//            FileUtils.forceMkdirParent(file);
//            FileWriter fw = new FileWriter(file);
//            fw.close();
//            PrintLog.e("file path = " + file.getAbsolutePath());
            // Issuer open TailsReader
            BlobStorageReader blobStorageReaderCfg = BlobStorageReader.openReader("default", tailsWriterConfig).get();
//            int blobStorageReaderHandleCfg = blobStorageReaderCfg.getBlobStorageReaderHandle();
            // Prover creates RevocationState
            PrintLog.e("getBlobStorageReaderHandle() = " + blobStorageReaderCfg.getBlobStorageReaderHandle());
            PrintLog.e("revocRegDefJson = " + revocRegDefJson);
            PrintLog.e("revocRegDeltaJson = " + revocRegDeltaJson);
            PrintLog.e("rev_reg_id = " + rev_reg_id);
            PrintLog.e("timestamp = " + timestamp);

            int timestamp1 = 100;
            PrintLog.e("timestamp = " + timestamp1);
//            try{
//                String revStateJson = Anoncreds.createRevocationState(blobStorageReaderCfg.getBlobStorageReaderHandle(),
//                        revocRegDefJson, revocRegDeltaJson, timestamp1, cred_rev_id).get();
//                PrintLog.e("revStateJson : " + revStateJson);
//            }catch(Exception ex){
//                ex.printStackTrace();
//            }
            try{
                PrintLog.e("proofRequest = " + proofRequest);
                PrintLog.e("requestedCredentialsJson = " + requestedCredentialsJson);
                PrintLog.e("did = " + did);
                PrintLog.e("schemaJson = " + schemaJson);
//                PrintLog.e("credDefJson = " + credDefJson);
                String credentialDefsJson = null;
                String schemasJson = null;
//                String revocStates = new JSONObject().put("ES52TT3zeLLvukKTjX8fMC:4:ES52TT3zeLLvukKTjX8fMC:3:CL:446:company_TAG_CD:CL_ACCUM:company_TAG_CD", new JSONObject().
//                        put(String.valueOf(timestamp), new JSONObject(revStateJson))).toString();
                String revocStates = new JSONObject().toString();
                StoreCredentialDataVo data;
                data = getRequestJobCredentialData(ctx);

                if(data != null){
                    schemaJson = data.getSchemaJson();
                    credDefJson = data.getCredDefJson();
                    credDefId = data.getCredentialDefId();
                    schemaId = data.getSchemaId();
                }
                PrintLog.e("schemaJson = " + schemaJson);
                try{
                    schemasJson = new JSONObject().put(schemaId, new JSONObject(schemaJson)).toString();
                    credentialDefsJson = new JSONObject().put(credDefId, new JSONObject(credDefJson)).toString();
                    PrintLog.e("schemasJson = " + schemasJson);
                }catch(JSONException e){
                    PrintLog.e("createProofJobRequest Error");
                }
                proverCreateProofreturn = Anoncreds.proverCreateProof(wallet, proofRequest, requestedCredentialsJson, did, schemasJson, credentialDefsJson, revocStates).get();
            }catch(ExecutionException | InterruptedException | IndyException e){
                PrintLog.e("createProofJobRequest error");
            }
            PrintLog.e("proverCreateProofreturn = " + proverCreateProofreturn);
//            PrintLog.e("credentialsForProofJson = " + credentialsForProofJson);
        }catch(JSONException | ExecutionException | InterruptedException | IndyException e){
            PrintLog.e("proverCreateProofreturn = " + proverCreateProofreturn);
            try{
                closeWallet();
            }catch(IndyException ex){
                PrintLog.e("proverCreateProofreturn = " + proverCreateProofreturn);
            }
        }
        return proverCreateProofreturn;
    }

    /**
     * indy did 삭제
     *
     * @param didData  DidDataVo
     * @param listener FinishListener
     */
    public void deleteIndyDid(DidDataVo didData, FinishListener listener){
        this.finishListener = listener;
        createPoolWorksForConfigJSON();
        createWallet();
        if(stewardDID == null){
            createStewardDID();
        }
        createTrustDID();
        deleteDID(didData.getDid(), didData.getPublicKey());
        try{
            closeWallet();
        }catch(Exception e){
            PrintLog.e("deleteIndyDid error ");
        }
        finishListener.finishOK(null);
    }

    /**
     * 사전 발행 VC 생성
     *
     * @param finishListener finishListener
     */
//    public void createPreVCIDCard(FinishListener finishListener){
//        this.finishListener = finishListener;
//        createPoolWorksForConfigJSON();
//        createWallet();
//        createStewardDID();
//        createTrustDID();
//        createGoverment();
////        createMobile();
//        createDid();
//        createIdCardCredential();
////        createPhoneCredention();
//        try{
//            closeWallet();
//            finishListener.finishOK(null);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }

    /**
     * 지갑 생성
     */
    public void createWallet(){
        try{
            Wallet.createWallet(wallet_config, WALLET_CREDENTIALS).get();

        }catch(ExecutionException | IndyException | InterruptedException e){
            PrintLog.e(e.getMessage());
            if(e.getMessage().indexOf("WalletExistsException") <= 0){
                PrintLog.e("error " + e.getMessage());
//                throw new RuntimeException(e);
                try{
                    closeWallet();
                }catch(Exception ex){
                    PrintLog.e("createWallet error ");
                }
            }
        }finally{
            if(!walletOpened){
                try{
                    wallet = Wallet.openWallet(wallet_config, WALLET_CREDENTIALS).get();
                }catch(ExecutionException | InterruptedException | IndyException e){
                    PrintLog.e("createWallet error");
                }
                walletOpened = true;
            }
        }
    }

    /**
     * DID 생성
     */
    private void createDid(){
        String seed = random32();
        PrintLog.e("SEED = " + seed);
        String myVerkey = null;
        String IDENTITY_JSON =
                new DidJSONParameters.CreateAndStoreMyDidJSONParameter(null, seed, null, null).toJson();
        try{
            if(!walletOpened){
                wallet = Wallet.openWallet(wallet_config, WALLET_CREDENTIALS).get();
                walletOpened = true;
            }
            myDidResult = Did.createAndStoreMyDid(wallet, "{}").get();
            PrintLog.e("myDidResult: " + myDidResult);
            myDID = myDidResult.getDid();
            myVerkey = myDidResult.getVerkey();
            PrintLog.e("My DID: " + myDID);
            PrintLog.e("verKey: " + myVerkey);
            PrintLog.e("trustAnchorDID:" + trustAnchorDID);
            String key = Did.keyForLocalDid(wallet, myDID).get();
            PrintLog.e("keyForLocalDid:" + key);
            String nymRequest = Ledger.buildNymRequest(trustAnchorDID, myDID, myVerkey, null, "").get();
            PrintLog.e("nymRequest: " + nymRequest);
            String signAndSubmitRequest = Ledger.signAndSubmitRequest(pool, wallet, trustAnchorDID, nymRequest).get();
            PrintLog.e("signAndSubmitRequest: " + signAndSubmitRequest);
//            String ddo = Ledger.buildGetDdoRequest(trustAnchorDID, myDid).get();
//            PrintLog.e("ddo: " + ddo);
//            nymRequest = Ledger.buildGetNymRequest(trustAnchorDID, myDid).get();
//            PrintLog.e("nymRequest: " + nymRequest);
        }catch(ExecutionException | IndyException | InterruptedException e){
            PrintLog.e("createDid error");
        }


//        try{
//            String getNymRequest = Ledger.buildGetNymRequest(goverDid, myDid).get();
//            PrintLog.e("getNymRequest: " + getNymRequest);
//            String getNymResponse = Ledger.signAndSubmitRequest(pool, wallet, goverDid, getNymRequest).get();
//            PrintLog.e("getNymResponse: " + getNymResponse);
//
//            String getAttrRequest = Ledger.buildGetAttribRequest(myDid, myDid, ENDPOINT, null, null).get();
//            PrintLog.e("getAttrRequest: " + getAttrRequest);
//
//            String getAttrRequest1 = Ledger.buildGetAttribRequest(goverDid, myDid, ENDPOINT, null, null).get();
//            PrintLog.e("getAttrRequest1: " + getAttrRequest1);
//            String getAttrResponse = Ledger.signAndSubmitRequest(pool, wallet, goverDid, getAttrRequest).get();
//            PrintLog.e("getAttrResponse: " + getAttrResponse);
//            String ddo = Ledger.buildGetDdoRequest(goverDid, myDid).get();
//            PrintLog.e("ddo: " + ddo);
//
////            String expandedVerkey = expandVerkey(getDid(myDid), Did.keyForLocalDid(wallet, myDid).get());
//            String getAttribResponse = PoolUtils.ensurePreviousRequestApplied(pool, getAttrRequest, response -> {
//                PrintLog.e("response = " + response);
//
//
//                //                JSONObject getAttribResponseObject = null;
//
////                try {
////                    getAttribResponseObject = new JSONObject(response);
//////                    return endpoint.equals(getAttribResponseObject.getJSONObject("result").getString("data"));
////                } catch (JSONException e) {
////                    e.printStackTrace();
////                    return false;
////                }
//                return true;
//            });
////            List<PublicKey> publicKeys;
////            List<Authentication> authentications;
////
////            String keyId = did + "#key-" + (++keyNum);
////
////            PublicKey publicKey = PublicKey.build(keyId, DIDDOCUMENT_PUBLICKEY_TYPES, null, expandedVerkey, null, null);
////            publicKeys = Collections.singletonList(publicKey);
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            e.printStackTrace();
//        }
//        try{
//            deleteWallet();
//        }catch(Exception e){
//            e.printStackTrace();
//        }

    }

    /**
     * did 삭제
     *
     * @param did    did
     * @param verKey verkey
     */
    private void deleteDID(String did, String verKey){
        // 1. Create My Did
        String myDid = removeMethod(did);
        PrintLog.e("myDid = " + myDid);
        String nymRequest = null;
        String submitRequest = null;
        String newVerkey = null;
        // 2. Build and send Nym Request
        try{
            String key = Did.keyForLocalDid(wallet, myDid).get();
            PrintLog.e("key = " + key);
            PrintLog.e("verkey = " + verKey);
        }catch(ExecutionException | InterruptedException | IndyException e){
            PrintLog.e("deleteDID error");
        }
        try{
            nymRequest = Ledger.buildNymRequest(trustAnchorDID, myDid, verKey, null, null).get();
            PrintLog.e("nymRequest = " + nymRequest);
            submitRequest = Ledger.signAndSubmitRequest(pool, wallet, trustAnchorDID, nymRequest).get();
            PrintLog.e("submitRequest = " + submitRequest);

            // 3. Start replacing of keys
            newVerkey = Did.replaceKeysStart(wallet, myDid, "{}").get();
            PrintLog.e("newVerkey = " + newVerkey);

            // 4. Build and send Nym Request with new key
            nymRequest = Ledger.buildNymRequest(myDid, myDid, newVerkey, null, null).get();
            PrintLog.e("nymRequest = " + nymRequest);
            submitRequest = Ledger.signAndSubmitRequest(pool, wallet, myDid, nymRequest).get();
            PrintLog.e("submitRequest = " + submitRequest);

            // 5. Apply replacing of keys
            Did.replaceKeysApply(wallet, myDid).get();

//            // 6. Send schema request
//            String schemaRequest = Ledger.buildSchemaRequest(myDid, SCHEMA_DATA).get();
//            Ledger.signAndSubmitRequest(pool, wallet, myDid, schemaRequest).get();
        }catch(ExecutionException | InterruptedException | IndyException e){
            PrintLog.e("deleteDID error");
        }
    }

    private void createTrustDID(){
        String TRUSTEE_SEED = "00000000000000000000000Trustee11";
        String TRUST_IDENTITY_JSON =
                new DidJSONParameters.CreateAndStoreMyDidJSONParameter(null, TRUSTEE_SEED, null, null).toJson();
        DidResults.CreateAndStoreMyDidResult trustDidResult;
        try{
            if(!walletOpened){
                wallet = Wallet.openWallet(wallet_config, WALLET_CREDENTIALS).get();
                walletOpened = true;
            }
            if(trustAnchorDID == null){
                trustDidResult = Did.createAndStoreMyDid(wallet, TRUST_IDENTITY_JSON).get();
                PrintLog.e("trustDidResult: " + trustDidResult);
                trustAnchorDID = trustDidResult.getDid();
                String verKey = trustDidResult.getVerkey();
                PrintLog.e("Trust DID: " + trustAnchorDID);
                PrintLog.e("Trust verKey: " + verKey);
                String key = Did.keyForLocalDid(wallet, trustAnchorDID).get();
                PrintLog.e("Trust keyForLocalDid:" + key);
                PrintLog.e("stewardDID :" + stewardDID);
                String nymRequest = Ledger.buildNymRequest(stewardDID, trustAnchorDID, verKey, null, "TRUST_ANCHOR").get();
                PrintLog.e("NYM request JSON:\n" + nymRequest);
                String nymResponseJson = Ledger.signAndSubmitRequest(pool, wallet, stewardDID, nymRequest).get();
                PrintLog.e("NYM transaction response:\n" + nymResponseJson);

            }
        }catch(ExecutionException | IndyException | InterruptedException e){
            PrintLog.e("createTrustDID error");
        }

    }

    private void createStewardDID(){
        String stewardSeed = "000000000000000000000000Steward2";
        String TRUST_IDENTITY_JSON =
                new DidJSONParameters.CreateAndStoreMyDidJSONParameter(null, stewardSeed, null, null).toJson();
        DidResults.CreateAndStoreMyDidResult stewardDidResult;
        try{
            if(!walletOpened){
                wallet = Wallet.openWallet(wallet_config, WALLET_CREDENTIALS).get();
                walletOpened = true;
            }
            if(stewardDID == null){
                stewardDidResult = Did.createAndStoreMyDid(wallet, TRUST_IDENTITY_JSON).get();
                PrintLog.e("stewardDidResult: " + stewardDidResult);
                stewardDID = stewardDidResult.getDid();
                String verKey = stewardDidResult.getVerkey();
                PrintLog.e("steward DID: " + stewardDID);
                PrintLog.e("steward verKey: " + verKey);
                String key = Did.keyForLocalDid(wallet, stewardDID).get();
                PrintLog.e("Trust keyForLocalDid:" + key);
                String nymRequest = Ledger.buildNymRequest(stewardDID, stewardDID, verKey, null, "STEWARD").get();
                PrintLog.e("NYM request JSON:\n" + nymRequest);
                String nymResponseJson = Ledger.signAndSubmitRequest(pool, wallet, stewardDID, nymRequest).get();
                PrintLog.e("NYM request JSON:\n" + nymResponseJson);
            }
        }catch(ExecutionException | IndyException | InterruptedException e){
            PrintLog.e("createStewardDID error");
        }

    }

    public void createPoolWorksForConfigJSON(){
        try{
            String poolname = createPoolLedgerConfig();
            PrintLog.e("poolname = " + poolname);
            Pool.setProtocolVersion(PROTOCOL_VERSION).get();

        }catch(IOException | InterruptedException | ExecutionException | IndyException e){
            if(e.getMessage().contains("A pool ledger configuration")){
                PrintLog.e("error " + e.getMessage());
                // ignore
            }else{
                PrintLog.e("error " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

        try{
            if(pool == null){
                pool = Pool.openPoolLedger(POLLNAME, "{}").get();
                if(pool != null){
                    PrintLog.e("pool open");
                }else{
                    PrintLog.e("pool open fail");
                }
            }
        }catch(ExecutionException | InterruptedException | IndyException e){
            PrintLog.e("createPoolWorksForConfigJSON error");
        }
    }

//    private void createIdCardSchemeTemp(){
//        String schemaVersion = "1.0";
//        String issuerSchemaName = "idCard";
//        String issuerSchemaAttributes = "[\"id\", \"name\", \"birth_date\", \"address\"]";
//        PrintLog.e("att : " + issuerSchemaAttributes);
//
//
//        Map<String, CredentialDataVo> credentialDate = new HashMap<>();
//        //create credential json
//        credentialDate.put("id", new CredentialDataVo(getDid(myDid)));
//        credentialDate.put("name", new CredentialDataVo("Gil-dong Hong"));
//        credentialDate.put("birth_date", new CredentialDataVo("1988-07-21"));
//        credentialDate.put("address", new CredentialDataVo("18"));
//        String CRED_VALUES = IITPApplication.gson.toJson(credentialDate);
//
//        String tag = "tag1";
//        PrintLog.e("CRED_VALUES = " + CRED_VALUES);
//        CredentialConfigVo credentialConfigVo = new CredentialConfigVo(false);
////        String defaultCredentialDefinitionConfig = "{\"support_revocation\":false}";
//        String defaultCredentialDefinitionConfig = IITPApplication.gson.toJson(credentialConfigVo);
//        AnoncredsResults.IssuerCreateSchemaResult createSchemaResult = null;
//
//        // Issuer create Scheme
//        try{
//            createSchemaResult = Anoncreds.issuerCreateSchema(goverDid, issuerSchemaName, schemaVersion, issuerSchemaAttributes).get();
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            e.printStackTrace();
//        }
//        String issuerSchemaId = createSchemaResult.getSchemaId();
//        String issuerSchema = createSchemaResult.getSchemaJson();
//        PrintLog.e("issuerSchemaId = " + issuerSchemaId);
//        PrintLog.e("issuerSchema = " + issuerSchema);
//
//        // Issuer create store CredentialDef
//        AnoncredsResults.IssuerCreateAndStoreCredentialDefResult issuerCreateGvtCredDefResult;
//        String issuerCredDefId = null;
//        String issuerCredDef = null;
//        try{
//            issuerCreateGvtCredDefResult = Anoncreds.issuerCreateAndStoreCredentialDef(wallet, goverDid, issuerSchema, tag, null, defaultCredentialDefinitionConfig).get();
//            issuerCredDefId = issuerCreateGvtCredDefResult.getCredDefId();
//            issuerCredDef = issuerCreateGvtCredDefResult.getCredDefJson();
//            PrintLog.e("issuerCredDefId = " + issuerCredDefId);
//            PrintLog.e("issuerCredDef = " + issuerCredDef);
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            e.printStackTrace();
//        }
//
//        // Issuer create Credential Offer
//        String issuerCredOffer = null;
//        try{
//            issuerCredOffer = Anoncreds.issuerCreateCredentialOffer(wallet, issuerCredDefId).get();
//            PrintLog.e("issuerCredOffer = " + issuerCredOffer);
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            e.printStackTrace();
//        }
//
//        // Prover create Master Secret
//        String materSecret = null;
//        try{
//            materSecret = Anoncreds.proverCreateMasterSecret(wallet, getDid(goverDid)).get();
//            PrintLog.e("materSecret = " + materSecret);
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            PrintLog.e("error " + e.getMessage());
//            if(e.getMessage().indexOf("Another master-secret with the specified") >= 0){
//                // ignore
//            }else{
//                PrintLog.e("error " + e.getMessage());
//                throw new RuntimeException(e);
//            }
//
//        }
//
//        // Prover create CredentialReq
//        String issuerCredReq = null;
//        String issuerCredReqMetaData = null;
//        AnoncredsResults.ProverCreateCredentialRequestResult createCredReqResult;
//        try{
//            if(materSecret == null){
//                materSecret = getDid(goverDid);
//            }
//            createCredReqResult = Anoncreds.proverCreateCredentialReq(wallet, myDid, issuerCredOffer, issuerCredDef, materSecret).get();
//            issuerCredReq = createCredReqResult.getCredentialRequestJson();
//            issuerCredReqMetaData = createCredReqResult.getCredentialRequestMetadataJson();
//            PrintLog.e("issuerCredReq = " + issuerCredReq);
//            PrintLog.e("issuerCredReqMetaData = " + issuerCredReqMetaData);
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            e.printStackTrace();
//        }
//        String credential = null;
//
//        // Issuer create Credential
//        try{
//            AnoncredsResults.IssuerCreateCredentialResult createCredResult =
//                    Anoncreds.issuerCreateCredential(wallet, issuerCredOffer, issuerCredReq, CRED_VALUES, null, -1).get();
//            credential = createCredResult.getCredentialJson();
//            PrintLog.e("credential = " + credential);
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            e.printStackTrace();
//        }
//
//        // Prover store Credential,  Prover get Credential
//        String storeCredentialId;
//        try{
//            storeCredentialId = Anoncreds.proverStoreCredential(wallet, credentialId1, issuerCredReqMetaData, credential, issuerCredDef, null).get();
//            PrintLog.e("Store credentialId = " + storeCredentialId);
//
//            String credentialData = Anoncreds.proverGetCredential(wallet, storeCredentialId).get();
//            PrintLog.e("Store credential = " + credentialData);
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            e.printStackTrace();
//            try{
//                closeWallet();
//            }catch(Exception ex){
//                ex.printStackTrace();
//            }
//        }
//
//        // Prover gets Credentials for Proof Request
//        String proofRequestJson = null;
//        try{
//            proofRequestJson = new JSONObject("{\n" +
//                    "  " +
//                    "\"nonce\": \"123432421212\",\n" +
//                    "  " +
//                    "\"name\": \"proof_req_1\",\n" +
//                    "  " +
//                    "\"version\": \"0.1\",\n" +
//                    "  " +
//                    "\"requested_attributes\": " +
//                    "{\n" +
//                    "    \"attr1_referent\": {" +
//                    "\"name\": \"birth_date\"" +
//                    "},\n" +
//                    "    " +
//                    "\"attr2_referent\": {" +
//                    "\"name\": \"name\"" +
//                    "},\n" +
//                    "    " +
//                    "\"attr3_referent\": {\"name\":\"id\"}\n" +
//                    "  " +
//                    "},\n" +
//                    "  " +
//                    "\"requested_predicates\":" +
//                    "{" +
//                    " \n" +
//                    "    \"predicate1_referent\":" +
//                    "{\n" +
//                    "      \"name\":\"address\",\n" +
//                    "      \"p_type\":\">=\",\n" +
//                    "      \"p_value\":18\n" +
//                    "    }\n" +
//                    "  " +
//                    "}\n" +
//                    "}").toString();
//
//            PrintLog.e("proofRequestJson = " + proofRequestJson);
//            String credentialsForProofJson = Anoncreds.proverGetCredentialsForProofReq(wallet, proofRequestJson).get();
//            PrintLog.e("credentialsForProofJson = " + credentialsForProofJson);
//        }catch(JSONException | IndyException | InterruptedException | ExecutionException e){
//            e.printStackTrace();
//            try{
//                closeWallet();
//            }catch(Exception ex){
//                e.printStackTrace();
//            }
//        }
//
//        // Prover create Proof
//        try{
//            testProverCreateProofWorks(issuerSchemaId, issuerSchema, issuerCredDefId, issuerCredDef, proofRequestJson, getDid(goverDid));
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//
//        try{
//            closeWallet();
//        }catch(Exception ex){
//            ex.printStackTrace();
//        }
//    }

//    private void createIdCardCredential(){
//        String schemaVersion = "1.0";
//        String issuerSchemaName = "gov_id_cred_def_id";
//        String issuerSchemaAttributes = "[\"id\", \"name\", \"birth_date\", \"address\"]";
//        PrintLog.e("att : " + issuerSchemaAttributes);
//
//
//        Map<String, CredentialDataVo> credentialDate = new HashMap<>();
//        //create credential json
//        credentialDate.put("id", new CredentialDataVo(getDid(sampleDID)));
//        credentialDate.put("name", new CredentialDataVo("Gil-dong Hong"));
//        credentialDate.put("birth_date", new CredentialDataVo("1988-07-21"));
////        credentialDate.put("address", new CredentialDataVo("218 Gajeong-ro, Yuseong-gu, Daejeon, 34129, KOREA"));
//        credentialDate.put("address", new CredentialDataVo("30"));
//        String CRED_VALUES = IITPApplication.gson.toJson(credentialDate);
//
//        String tag = "tag1";
//        PrintLog.e("CRED_VALUES = " + CRED_VALUES);
//        CredentialConfigVo credentialConfigVo = new CredentialConfigVo(false);
////        String defaultCredentialDefinitionConfig = "{\"support_revocation\":false}";
//        String defaultCredentialDefinitionConfig = IITPApplication.gson.toJson(credentialConfigVo);
//        AnoncredsResults.IssuerCreateSchemaResult createSchemaResult = null;
//
//        // Issuer create Scheme
//        try{
//            createSchemaResult = Anoncreds.issuerCreateSchema(goverDid, issuerSchemaName, schemaVersion, issuerSchemaAttributes).get();
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            PrintLog.e("createIdCardCredential error");
//        }
//        String issuerSchemaId = createSchemaResult.getSchemaId();
//        String issuerSchema = createSchemaResult.getSchemaJson();
//        PrintLog.e("issuerSchemaId = " + issuerSchemaId);
//        PrintLog.e("issuerSchema = " + issuerSchema);
//        // Issuer posts Schema to Ledger
//        String schemaRequest = null;
//        try{
//            schemaRequest = Ledger.buildSchemaRequest(goverDid, issuerSchema).get();
//            PrintLog.e("schemaRequest = " + schemaRequest);
//            Ledger.signAndSubmitRequest(pool, wallet, goverDid, schemaRequest).get();
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            PrintLog.e("createIdCardCredential error");
//        }
//        // Issuer create store CredentialDef
//        AnoncredsResults.IssuerCreateAndStoreCredentialDefResult issuerCreateGvtCredDefResult;
//        String issuerCredDefId = null;
//        String issuerCredDef = null;
//        try{
//            issuerCreateGvtCredDefResult = Anoncreds.issuerCreateAndStoreCredentialDef(wallet, goverDid, issuerSchema, tag, null, defaultCredentialDefinitionConfig).get();
//            issuerCredDefId = issuerCreateGvtCredDefResult.getCredDefId();
//            issuerCredDef = issuerCreateGvtCredDefResult.getCredDefJson();
//            PrintLog.e("issuerCredDefId = " + issuerCredDefId);
//            PrintLog.e("issuerCredDef = " + issuerCredDef);
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            PrintLog.e("createIdCardCredential error");
//        }
//
//        // Issuer create Credential Offer
//        String issuerCredOffer = null;
//        try{
//            issuerCredOffer = Anoncreds.issuerCreateCredentialOffer(wallet, issuerCredDefId).get();
//            PrintLog.e("issuerCredOffer = " + issuerCredOffer);
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            PrintLog.e("createIdCardCredential error");
//        }
//
//        // Prover create Master Secret
//        String materSecret = null;
//        try{
//            materSecret = Anoncreds.proverCreateMasterSecret(wallet, myDID).get();
//            PrintLog.e("materSecret = " + materSecret);
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            PrintLog.e("error " + e.getMessage());
//            if(e.getMessage().indexOf("Another master-secret with the specified") >= 0){
//                // ignore
//            }else{
//                PrintLog.e("error " + e.getMessage());
//                throw new RuntimeException(e);
//            }
//
//        }
//
//        // Prover create CredentialReq
//        String issuerCredReq = null;
//        String issuerCredReqMetaData = null;
//        AnoncredsResults.ProverCreateCredentialRequestResult createCredReqResult;
//        try{
////            if(materSecret == null){
////                materSecret = myDID;
////            }
//            createCredReqResult = Anoncreds.proverCreateCredentialReq(wallet, myDID, issuerCredOffer, issuerCredDef, materSecret).get();
//            issuerCredReq = createCredReqResult.getCredentialRequestJson();
//            issuerCredReqMetaData = createCredReqResult.getCredentialRequestMetadataJson();
//            PrintLog.e("issuerCredReq = " + issuerCredReq);
//            PrintLog.e("issuerCredReqMetaData = " + issuerCredReqMetaData);
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            PrintLog.e("createIdCardCredential error");
//        }
//
//        // Issuer create Credential
//        String credential = null;
//        try{
//            AnoncredsResults.IssuerCreateCredentialResult createCredResult =
//                    Anoncreds.issuerCreateCredential(wallet, issuerCredOffer, issuerCredReq, CRED_VALUES, null, -1).get();
//            credential = createCredResult.getCredentialJson();
//            PrintLog.e("credential = " + credential);
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            PrintLog.e("createIdCardCredential error");
//        }
//
//        // Prover store Credential,  Prover get Credential
//        String storeCredentialId;
//        long nonce = System.currentTimeMillis();
//        try{
//            storeCredentialId = Anoncreds.proverStoreCredential(wallet, String.valueOf(nonce), issuerCredReqMetaData, credential, issuerCredDef, null).get();
//            PrintLog.e("Store credentialId = " + storeCredentialId);
////
//            String credentialData = Anoncreds.proverGetCredential(wallet, storeCredentialId).get();
//            PrintLog.e("credentialData = " + credentialData);
//
//            // Prover gets Credentials for Proof Request
//            String proofRequestJson = null;
//            try{
//                proofRequestJson = new JSONObject("{\n" +
//                        "  " +
//                        "\"nonce\": \"1234324212123\",\n" +
//                        "  " +
//                        "\"name\": \"proof_req_1\",\n" +
//                        "  " +
//                        "\"version\": \"0.1\",\n" +
//                        "  " +
//                        "\"requested_attributes\": " +
//                        "{\n" +
//                        "    \"attr1_referent\": {" +
//                        "\"name\": \"birth_date\"" +
//                        "},\n" +
//                        "    " +
//                        "\"attr2_referent\": {" +
//                        "\"name\": \"name\"" +
//                        "},\n" +
//                        "    " +
//                        "\"attr3_referent\": {\"name\":\"id\"}\n" +
//                        "  " +
//                        "},\n" +
//                        "  " +
//                        "\"requested_predicates\":" +
//                        "{" +
//                        " \n" +
//                        "    \"predicate1_referent\":" +
//                        "{\n" +
//                        "      \"name\":\"address\",\n" +
//                        "      \"p_type\":\">=\",\n" +
//                        "      \"p_value\":18\n" +
//                        "    }\n" +
//                        "  " +
//                        "}\n" +
//                        "}").toString();
//
//                PrintLog.e("proofRequestJson = " + proofRequestJson);
//
//                String credentialsForProofJson = Anoncreds.proverGetCredentialsForProofReq(wallet, proofRequestJson).get();
//                PrintLog.e("credentialsForProofJson = " + credentialsForProofJson);
//            }catch(JSONException | IndyException | InterruptedException | ExecutionException e){
//                e.printStackTrace();
//                try{
//                    closeWallet();
//                }catch(Exception ex){
//                    e.printStackTrace();
//                }
//            }
//
//            PrintLog.e("issuerSchemaId = " + issuerSchemaId);
//            PrintLog.e("issuerSchema = " + issuerSchema);
//            PrintLog.e("issuerCredDefId = " + issuerCredDefId);
//            PrintLog.e("issuerCredDef = " + issuerCredDef);
//            PrintLog.e("proofRequestJson = " + proofRequestJson);
//
//
//            // Prover create Proof
//            try{
//                testProverCreateProofWorks(issuerSchemaId, issuerSchema, issuerCredDefId, issuerCredDef, proofRequestJson, goverDid);
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//
//        }catch(Exception e){
//            e.printStackTrace();
//            try{
//                closeWallet();
//            }catch(Exception ex){
//                ex.printStackTrace();
//            }
//        }
//
//    }


//    private void createPhoneCredention(){
//        String schemaVersion = "1.0";
//        String issuerSchemaName = "phone";
//        String issuerSchemaAttributes = "[\"id\", \"name\", \"phone_num\"]";
//        PrintLog.e("att : " + issuerSchemaAttributes);
//
//        Map<String, CredentialDataVo> credentialDate = new HashMap<>();
//        //create credential json
//        credentialDate.put("id", new CredentialDataVo(getDid(sampleDID)));
//        credentialDate.put("name", new CredentialDataVo("Gil-dong Hong"));
//        credentialDate.put("phone_num", new CredentialDataVo("010-1234-5678"));
//        String CRED_VALUES = IITPApplication.gson.toJson(credentialDate);
//
//        String tag = "tag1";
//        PrintLog.e("CRED_VALUES = " + CRED_VALUES);
//        CredentialConfigVo credentialConfigVo = new CredentialConfigVo(false);
////        String defaultCredentialDefinitionConfig = "{\"support_revocation\":false}";
//        String defaultCredentialDefinitionConfig = IITPApplication.gson.toJson(credentialConfigVo);
//        AnoncredsResults.IssuerCreateSchemaResult createSchemaResult = null;
//
//        // Issuer create Scheme
//        try{
//            createSchemaResult = Anoncreds.issuerCreateSchema(mobileDid, issuerSchemaName, schemaVersion, issuerSchemaAttributes).get();
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            e.printStackTrace();
//        }
//        String issuerSchemaId = createSchemaResult.getSchemaId();
//        String issuerSchema = createSchemaResult.getSchemaJson();
//        PrintLog.e("issuerSchemaId = " + issuerSchemaId);
//        PrintLog.e("issuerSchema = " + issuerSchema);
//        // Issuer posts Schema to Ledger
//        String schemaRequest = null;
//        try{
//            schemaRequest = Ledger.buildSchemaRequest(mobileDid, issuerSchema).get();
//            PrintLog.e("schemaRequest = " + schemaRequest);
//            Ledger.signAndSubmitRequest(pool, wallet, mobileDid, schemaRequest).get();
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            e.printStackTrace();
//        }
//        // Issuer create store CredentialDef
//        AnoncredsResults.IssuerCreateAndStoreCredentialDefResult issuerCreateGvtCredDefResult;
//        String issuerCredDefId = null;
//        String issuerCredDef = null;
//        try{
//            issuerCreateGvtCredDefResult = Anoncreds.issuerCreateAndStoreCredentialDef(wallet, mobileDid, issuerSchema, tag, null, defaultCredentialDefinitionConfig).get();
//            issuerCredDefId = issuerCreateGvtCredDefResult.getCredDefId();
//            issuerCredDef = issuerCreateGvtCredDefResult.getCredDefJson();
//            PrintLog.e("issuerCredDefId = " + issuerCredDefId);
//            PrintLog.e("issuerCredDef = " + issuerCredDef);
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            e.printStackTrace();
//        }
//
//        // Issuer create Credential Offer
//        String issuerCredOffer = null;
//        try{
//            issuerCredOffer = Anoncreds.issuerCreateCredentialOffer(wallet, issuerCredDefId).get();
//            PrintLog.e("issuerCredOffer = " + issuerCredOffer);
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            e.printStackTrace();
//        }
//
//        // Prover create Master Secret
//        String materSecret = null;
//        try{
//            materSecret = Anoncreds.proverCreateMasterSecret(wallet, getDid(mobileDid)).get();
//            PrintLog.e("materSecret = " + materSecret);
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            PrintLog.e("error " + e.getMessage());
//            if(e.getMessage().indexOf("Another master-secret with the specified") >= 0){
//                // ignore
//            }else{
//                PrintLog.e("error " + e.getMessage());
//                throw new RuntimeException(e);
//            }
//
//        }
//
//        // Prover create CredentialReq
//        String issuerCredReq = null;
//        String issuerCredReqMetaData = null;
//        AnoncredsResults.ProverCreateCredentialRequestResult createCredReqResult;
//        try{
//            if(materSecret == null){
//                materSecret = getDid(mobileDid);
//            }
//            createCredReqResult = Anoncreds.proverCreateCredentialReq(wallet, sampleDID, issuerCredOffer, issuerCredDef, materSecret).get();
//            issuerCredReq = createCredReqResult.getCredentialRequestJson();
//            issuerCredReqMetaData = createCredReqResult.getCredentialRequestMetadataJson();
//            PrintLog.e("issuerCredReq = " + issuerCredReq);
//            PrintLog.e("issuerCredReqMetaData = " + issuerCredReqMetaData);
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            e.printStackTrace();
//        }
//
//        // Issuer create Credential
//        String credential = null;
//        try{
//            AnoncredsResults.IssuerCreateCredentialResult createCredResult =
//                    Anoncreds.issuerCreateCredential(wallet, issuerCredOffer, issuerCredReq, CRED_VALUES, null, -1).get();
//            credential = createCredResult.getCredentialJson();
//            PrintLog.e("credential = " + credential);
//        }catch(ExecutionException | InterruptedException | IndyException e){
//            e.printStackTrace();
//        }
//        // Prover store Credential,  Prover get Credential
////        String storeCredentialId;
////        long nonce = System.currentTimeMillis();
////        try{
////            storeCredentialId = Anoncreds.proverStoreCredential(wallet, String.valueOf(nonce), issuerCredReqMetaData, credential, issuerCredDef, null).get();
////            PrintLog.e("Store credentialId = " + storeCredentialId);
////
////            String credentialData = Anoncreds.proverGetCredential(wallet, storeCredentialId).get();
////            PrintLog.e("Store credential = " + credentialData);
////        }catch(ExecutionException | InterruptedException | IndyException e){
////            e.printStackTrace();
////            try{
////                closeWallet();
////            }catch(Exception ex){
////                ex.printStackTrace();
////            }
////        }
//
//    }

    private String getDid(String id){
        String did = null;
        did = "did:sov:" + id;
        return did;
    }

    private String removeMethod(String did){
        String[] temp = did.split(":");
        if(temp.length != 1){
            did = temp[2];
        }
        return did;
    }

//    /**
//     * 행안부 DID 생성
//     */
//    public void createGoverment(){
//        String SEED = "0000000000000000000000000GOVER01";
//        String IDENTITY_JSON =
//                new DidJSONParameters.CreateAndStoreMyDidJSONParameter(null, SEED, null, null).toJson();
//
//        DidResults.CreateAndStoreMyDidResult myDidResult = null;
//        try{
//            PrintLog.e("GOVER_IDENTITY_JSON = " + IDENTITY_JSON);
//            myDidResult = Did.createAndStoreMyDid(wallet, IDENTITY_JSON).get();
//            goverDid = myDidResult.getDid();
//            String verKey = myDidResult.getVerkey();
//            PrintLog.e("===================> GOV DID:" + goverDid);
//            PrintLog.e("===================>verKey:" + verKey);
//            String key = Did.keyForLocalDid(wallet, goverDid).get();
//            PrintLog.e("keyForLocalDid:" + key);
//
//            String nymRequest = Ledger.buildNymRequest(trustAnchorDID, goverDid, verKey, null, "").get();
//            PrintLog.e("===================>nymRequest:" + nymRequest);
////            String signedMessage = Ledger.signRequest(wallet, goverDid, nymRequest).get();
////            PrintLog.e("===================>signedMessage:" + signedMessage);
//            String signAndSubmitRequest = Ledger.signAndSubmitRequest(pool, wallet, trustAnchorDID, nymRequest).get();
//            PrintLog.e("===================>signAndSubmitRequest: " + signAndSubmitRequest);
//        }catch(InterruptedException | ExecutionException | IndyException e){
//            e.printStackTrace();
//        }
//    }

//    /**
//     * 통산시 DID 생성
//     */
//    private void createMobile(){
//        String SEED = "00000000000000000000000000MOBILE";
//        String IDENTITY_JSON =
//                new DidJSONParameters.CreateAndStoreMyDidJSONParameter(null, SEED, null, null).toJson();
//        DidResults.CreateAndStoreMyDidResult myDidResult = null;
//        try{
//            PrintLog.e("MY1_IDENTITY_JSON = " + IDENTITY_JSON);
//            myDidResult = Did.createAndStoreMyDid(wallet, IDENTITY_JSON).get();
//            mobileDid = myDidResult.getDid();
//            String verKey = myDidResult.getVerkey();
//            PrintLog.e("===================> MOBILE DID:" + mobileDid);
//            PrintLog.e("===================>verKey:" + verKey);
//            String key = Did.keyForLocalDid(wallet, mobileDid).get();
//            PrintLog.e("keyForLocalDid:" + key);
//
//            String nymRequest = Ledger.buildNymRequest(trustAnchorDID, mobileDid, verKey, null, "TRUSTEE").get();
//            PrintLog.e("===================>nymRequest:" + nymRequest);
//            String signedMessage = Ledger.signRequest(wallet, mobileDid, nymRequest).get();
//            PrintLog.e("===================>signedMessage:" + signedMessage);
//            String signAndSubmitRequest = Ledger.signAndSubmitRequest(pool, wallet, trustAnchorDID, nymRequest).get();
//            PrintLog.e("===================>signAndSubmitRequest: " + signAndSubmitRequest);
//        }catch(InterruptedException | ExecutionException | IndyException e){
//            e.printStackTrace();
//        }
//    }

    /**
     * 지갑 닫기
     *
     * @throws Exception
     */
    public void closeWallet() throws IndyException{
        PrintLog.e("close wallet");

        if(wallet != null){
            try{
                wallet.closeWallet().get();
            }catch(ExecutionException | InterruptedException e){
                PrintLog.e("close wallet");
            }
//            Wallet.deleteWallet(wallet_config, WALLET_CREDENTIALS).get();
            walletOpened = false;
        }
//        if(pool != null){
//            pool.closePoolLedger().get();
//        }
//
//        PrintLog.e("finish delete wallet");
    }

    //make proof json

    /**
     * proof 생성 테스트
     *
     * @param gvtSchemaId
     * @param gvtSchema
     * @param issuer1gvtCredDefId
     * @param issuer1gvtCredDef
     * @param proofRequest
     * @param masterSecretId
     * @throws Exception
     */
    private void testProverCreateProofWorks(String gvtSchemaId, String gvtSchema, String issuer1gvtCredDefId, String issuer1gvtCredDef, String proofRequest, String masterSecretId) throws Exception{
        CredentialsSearchForProofReq credentialsSearch = CredentialsSearchForProofReq.open(wallet, proofRequest, null).get();
        JSONArray credentialsForAttribute1 = new JSONArray(credentialsSearch.fetchNextCredentials("attr1_referent", 100).get());
        String credentialIdForAttribute1 = credentialsForAttribute1.getJSONObject(0).getJSONObject("cred_info").getString("referent");
        PrintLog.e("credentialIdForAttribute1 = " + credentialIdForAttribute1);
        JSONArray credentialsForAttribute2 = new JSONArray(credentialsSearch.fetchNextCredentials("attr2_referent", 100).get());
        String credentialIdForAttribute2 = credentialsForAttribute2.getJSONObject(0).getJSONObject("cred_info").getString("referent");
        PrintLog.e("credentialIdForAttribute2 = " + credentialIdForAttribute2);
        JSONArray credentialsForAttribute3 = new JSONArray(credentialsSearch.fetchNextCredentials("attr3_referent", 100).get());
        String credentialIdForAttribute3 = credentialsForAttribute2.getJSONObject(0).getJSONObject("cred_info").getString("referent");
        PrintLog.e("credentialsForAttribute3 = " + credentialIdForAttribute3);
//		assertEquals(0, credentialsForAttribute3.length());

        JSONArray credentialsForPredicate = new JSONArray(credentialsSearch.fetchNextCredentials("predicate1_referent", 100).get());
        String credentialIdForPredicate = credentialsForPredicate.getJSONObject(0).getJSONObject("cred_info").getString("referent");
        PrintLog.e("credentialIdForPredicate = " + credentialIdForPredicate);

        credentialsSearch.close();
        String requestedCredentialsJson = String.format("{\n" +
                "  " +
                "\"self_attested_attributes\":{},\n" +
                "  " +
                "\"requested_attributes\":{\n" +
                "    \"attr1_referent\":{\"cred_id\":\"%s\", \"revealed\":true},\n" +
                "    \"attr2_referent\":{\"cred_id\":\"%s\", \"revealed\":true},\n" +
                "    \"attr3_referent\":{\"cred_id\":\"%s\", \"revealed\":true}\n" +
                "  },\n" +
                "  " +
                "\"requested_predicates\":{\n" +
                "    \"predicate1_referent\":{\"cred_id\":\"%s\"}\n" +
                "  }\n" +
                "}", credentialIdForAttribute1, credentialIdForAttribute2, credentialIdForAttribute3, credentialIdForPredicate);
        PrintLog.e("requestedCredentialsJson = " + requestedCredentialsJson);
        String credentialsForProofJson = Anoncreds.proverGetCredentialsForProofReq(wallet, proofRequest).get();
        PrintLog.e("credentialsForProofJson = " + credentialsForProofJson);
        String schemasJson = new JSONObject().put(gvtSchemaId, new JSONObject(gvtSchema)).toString();
        String credentialDefsJson = new JSONObject().put(issuer1gvtCredDefId, new JSONObject(issuer1gvtCredDef)).toString();
        String revocStatesJson = new JSONObject().toString();
        PrintLog.e("proofRequest = " + proofRequest);
        PrintLog.e("requestedCredentialsJson = " + requestedCredentialsJson);
        PrintLog.e("requestedCredentialsJson = " + new JSONObject(requestedCredentialsJson).toString());
        PrintLog.e("masterSecretId = " + masterSecretId);
        PrintLog.e("schemasJson = " + schemasJson);
        PrintLog.e("credentialDefsJson = " + credentialDefsJson);
        PrintLog.e("revocStatesJson = " + revocStatesJson);

        PrintLog.i("proverCreateProof start");
        String proofJson = Anoncreds.proverCreateProof(wallet, proofRequest, new JSONObject(requestedCredentialsJson).toString(),
                masterSecretId, schemasJson, credentialDefsJson, revocStatesJson).get();
        PrintLog.e("proofJson = " + proofJson);
        String signedMessage = Ledger.signRequest(wallet, myDID, proofJson).get();
        PrintLog.e("signedMessage = " + signedMessage);
    }

//    private VerifiableCredential vcCreateTest(String credential, String typeName, String did, String key){
//        String vcJwt = null;
//        // 만료일
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.YEAR, 2);
//        // VC 발급 - Issuer 가 발급
//        VerifiableCredential vc = new VerifiableCredential();
//        vc.setTypes(Arrays.asList("ZKP_CREDENTIAL", typeName));
//        vc.setExpirationDate(calendar.getTime());
//        vc.setIssuanceDate(new Date());
//        ArrayList<String> subject = new ArrayList();
//        String base64String = Base64.encodeToString(credential.getBytes(), Base64.NO_WRAP);
//        PrintLog.e("base64String = "+base64String);
//        PrintLog.e("decode 64 = "+new String(Base64.decode(base64String, Base64.NO_WRAP)));
//        subject.add(base64String);
//        vc.setCredentialSubject(subject);
//        try{
//            String signedVc = new MetadiumSigner(did, ISSUER_KID, key).sign(vc); // issuer 의 DID 로 서명
//            vcJwt = signedVc;
//            PrintLog.e("VC = " + signedVc);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        PrintLog.e("vc = "+vc.toJSONString());
//        return vc;
//    }

    /**
     * 랜덤키생성
     *
     * @return
     */
    private String random32(){
        Random rnd = new Random();
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < 32; i++){
            // rnd.nextBoolean() 는 랜덤으로 true, false 를 리턴. true일 시 랜덤 한 소문자를, false 일 시 랜덤 한 숫자를 StringBuffer 에 append 한다.
            if(rnd.nextBoolean()){
                buf.append((char) ((int) (rnd.nextInt(26)) + 97));
            }else{
                buf.append((rnd.nextInt(10)));
            }
        }
        return buf.toString();
    }

    /**
     * DID 저장
     *
     * @param context  context
     * @param nickName 별칭
     * @param type     체인 타입
     */
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
        String publicKey = myDidResult.getVerkey();
        String did = getDid(myDidResult.getDid());
        PrintLog.e("did = " + did);
        PrintLog.e("public Key  = " + publicKey);
        PrintLog.e("time = " + time);
        PrintLog.e("contract = " + type);
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
        preferenceUtil.setIndyDID(myDidResult.getDid());
        didDataVo = new DidDataVo(did, "", publicKey, time, nickName, type, false, "", "");
        didList.add(didDataVo);
        MainActivity.setDidList(didList, context);
    }

    public byte[] indySignMessage(String verkey, byte[] message){
        byte[] signature = new byte[0];
        createPoolWorksForConfigJSON();
        createWallet();
        try{
            signature = Crypto.cryptoSign(wallet, verkey, message).get();
        }catch(ExecutionException | InterruptedException | IndyException e){
            PrintLog.e("indySignMessage error");
        }
        return signature;
    }

    public boolean indyVerifyMessage(String verkey, byte[] message, byte[] signature){
        boolean rtn = false;
//        createPoolWorksForConfigJSON();
//        createWallet();
        try{
            rtn = Crypto.cryptoVerify(verkey, message, signature).get();
        }catch(ExecutionException | InterruptedException | IndyException e){
            PrintLog.e("indyVerifyMessage error");
        }
        return rtn;
    }

//    public void ProverSearchCredentialsForProofRequestWorks(String DID, String schemaId ,String request){
//        CredentialsSearchForProofReq credentialsSearch;
//        try{
//            credentialsSearch = CredentialsSearchForProofReq.open(wallet, new JSONObject(request).toString(), null).get();
//            JSONArray credentialsForAttribute1 = new JSONArray(credentialsSearch.fetchNextCredentials("attr1_referent", 100).get());
//            credentialsSearch.close();
//            String getSchemaRequest = Ledger.buildGetSchemaRequest(DID, String.valueOf(schemaId)).get();
//            String getSchemaRequestResponse = Ledger.submitRequest(pool, getSchemaRequest).get();
//            String getCredDefRequest = Ledger.buildGetCredDefRequest(DID, schemaId).get();
//            String getCredDefRequestResponse = Ledger.submitRequest(pool, getCredDefRequest).get();
////            String getRevocRegrequest = Ledger.buildGetRevocRegDefRequest(DID, "RevocRegID").get();
////            String getgetRevocRegrequest = Ledger.submitRequest(pool, getRevocRegrequest).get();
////            String getRevRegDeltaRequest = Ledger.buildGetRevocRegDeltaRequest(DID, "revRegId", -1, 100).get();//todo
////            String getRevRegDeltaResponse = Ledger.submitRequest(pool, getRevRegDeltaRequest).get();
////            String revStateJson = Anoncreds.createRevocationState(blobStorageReaderHandleCfg, revRegDef, revRegDelta, timestamp, credRevId).get();
//            String revocStatesJson = new JSONObject().toString();
//            PrintLog.e("revocStatesJson = "+revocStatesJson);
//            String requestedCredentialsJson = String.format("{" +
//                    "\"self_attested_attributes\":{}," +
//                    "\"requested_attributes\":{\"attr1_referent\":{\"cred_id\":\"%s\", \"revealed\":true}}," +
//                    "\"requested_predicates\":{\"predicate1_referent\":{\"cred_id\":\"%s\"}}" +
//                    "}", "id1", "id2");
//            String proofJson = Anoncreds.proverCreateProof(wallet, request, new JSONObject(requestedCredentialsJson).toString(),
//                    DID, schemasJson, credentialDefsJson, revocStatesJson).get();
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }
}
