package com.iitp.iitp_demo.chain;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import androidx.appcompat.app.AlertDialog;

import com.google.gson.reflect.TypeToken;
import com.iitp.core.contract.IdentityRegistry;
import com.iitp.core.crypto.KeyManager;
import com.iitp.core.protocol.MetaDelegator;
import com.iitp.core.util.Web3jUtils;
import com.iitp.core.wapper.NotSignTransactionManager;
import com.iitp.core.wapper.ZeroContractGasProvider;
import com.iitp.iitp_demo.FinishListener;
import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.MainActivity;
import com.iitp.iitp_demo.activity.model.BlockChainType;
import com.iitp.iitp_demo.activity.model.DidDataVo;
import com.iitp.iitp_demo.util.CommonPreference;
import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.util.AsyncTaskResult;

import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.MnemonicUtils;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.iitp.iitp_demo.Constants.DID_LIST;

public class Metadium{
    private static Metadium instance;
    private String privateKey;

    private Metadium(){

    }

    public static synchronized Metadium getInstance(){
        if(instance == null){
            instance = new Metadium();
        }
        return instance;
    }

    FinishListener finishListener;
    public void createMetaDID(Activity activity, FinishListener listener){
        try{
            finishListener = listener;
            AsyncTaskResult<String> result =
                    new CreateMetaIdTask(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (String) null).get();

        }catch(ExecutionException | InterruptedException e){
            e.printStackTrace();
        }
    }

    public void deleteMetaDID(Activity activity, DidDataVo didData, FinishListener listener){
        try{
            AsyncTaskResult<String> result =
                    new DeleteMetaIdTask(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, didData.getDid()).get();
            listener.finishOK(null);

        }catch(ExecutionException | InterruptedException e){
            e.printStackTrace();
        }
    }

    public class CreateMetaIdTask extends AsyncTask<String, Void, AsyncTaskResult<String>>{
        @SuppressLint("StaticFieldLeak")
        private Activity activity;

        CreateMetaIdTask(Activity activity){
            this.activity = activity;
        }

        @Override
        protected void onPreExecute(){
        }

        @SuppressLint("WrongThread")
        @Override
        protected AsyncTaskResult<String> doInBackground(String... strings){

            String managementKey;
            String mnemonic;
            ECKeyPair keyPair;
            MetaDelegator metaDelegator = new MetaDelegator(false);
            // key 가 존재하지 않으면 key 를 생성한다.
//            if(managementKey == null){
            byte[] initialEntropy = new byte[16];
            new SecureRandom().nextBytes(initialEntropy);

            mnemonic = MnemonicUtils.generateMnemonic(initialEntropy);
            byte[] entropy = MnemonicUtils.generateEntropy(mnemonic);
            String normalizedMnemonic = MnemonicUtils.generateMnemonic(entropy);
            byte[] seed = MnemonicUtils.generateSeed(normalizedMnemonic, null);
            Bip32ECKeyPair master = Bip32ECKeyPair.generateKeyPair(seed);
            keyPair = Bip32ECKeyPair.deriveKeyPair(master, KeyManager.BIP44_META_PATH);
            managementKey = Numeric.prependHexPrefix(Keys.getAddress(keyPair.getPublicKey()));
//            }
//            else{
//
//                mnemonic = KeyManager.getInstance().getManagementMnemonic(activity);
//                byte[] entropy = MnemonicUtils.generateEntropy(mnemonic);
//                String normalizedMnemonic = MnemonicUtils.generateMnemonic(entropy);
//                byte[] seed = MnemonicUtils.generateSeed(normalizedMnemonic, null);
//                Bip32ECKeyPair master = Bip32ECKeyPair.generateKeyPair(seed);
//                keyPair = Bip32ECKeyPair.deriveKeyPair(master, KeyManager.BIP44_META_PATH);
//            }
            Boolean hasId;
            IdentityRegistry identityRegistry = IdentityRegistry.load(
                    metaDelegator.getAllServiceAddress().identityRegistry,
                    metaDelegator.getWeb3j(),
                    new NotSignTransactionManager(metaDelegator.getWeb3j()),
                    new ZeroContractGasProvider()
            );
            try{
                // ID가 등록되어 있는지 확인한다.
                hasId = identityRegistry.hasIdentity(managementKey).send();
            }catch(Exception e){
                return new AsyncTaskResult<>(e);
            }

            TransactionReceipt transactionReceipt;
            String transactionHash;
            try{
                // MetaID 생성을 Proxy 에 요청한다.
                transactionHash = metaDelegator.createIdentityDelegated(keyPair);
            }catch(Exception e){
                return new AsyncTaskResult<>(e);
            }

            try{
                if(!hasId){
                    // Get transaction receipt
                    transactionReceipt = Web3jUtils.ethGetTransactionReceipt(metaDelegator.getWeb3j(), transactionHash);
                    if(transactionReceipt.getStatus().equals("0x1")){
                        // receipt 의 로그에서 MetaID 생성 event 를 찾어 MetaID 로 사용될 ein 값을 조회한다.
                        List<IdentityRegistry.IdentityCreatedEventResponse> responses = identityRegistry.getIdentityCreatedEvents(transactionReceipt);
                        if(responses.size() > 0){
                            String did = metaDelegator.einToDid(responses.get(0).ein);

                            // Identity 및 key 저장
                            KeyManager keyManager = new KeyManager(did);
//                            if(KeyManager.getInstance().setIdentity(activity, did, mnemonic)){
                            if(keyManager.setIdentity(activity, did, mnemonic)){
                                try{
                                    String result = metaDelegator.addPublicKeyDelegated(activity, did);
                                    TransactionReceipt addPublicReceipt = Web3jUtils.ethGetTransactionReceipt(metaDelegator.getWeb3j(), result);
                                    if(!transactionReceipt.getStatus().equals("0x1")){
                                        return new AsyncTaskResult<>(new Exception("Add Publickey TransactionReceipt.status is " + addPublicReceipt.getStatus()));
                                    }
                                }catch(Exception e){
                                    return new AsyncTaskResult<>(e);
                                }
                                return new AsyncTaskResult<>(did);
                            }else{
                                return new AsyncTaskResult<>(new Exception("Error save Meta ID"));
                            }
                        }else{
                            return new AsyncTaskResult<>(new Exception("TransactionReceipt.logs is empty"));
                        }
                    }else{
                        return new AsyncTaskResult<>(new Exception("TransactionReceipt.status is " + transactionReceipt.getStatus()));
                    }
                }else{
                    BigInteger ein;
                    ein = identityRegistry.getEIN(managementKey).send();
//                    String metaId = Numeric.toHexStringWithPrefixZeroPadded(ein, 32);
                    String did = metaDelegator.einToDid(ein);
                    // key 저장
                    KeyManager keyManager = new KeyManager(did);
                    if(keyManager.setIdentity(activity, did, managementKey)){
//                        if(KeyManager.getInstance().setIdentity(activity, did, managementKey)){
                        if(mnemonic != null){
                            try{
                                String result = metaDelegator.addPublicKeyDelegated(activity, did);
                            }catch(Exception e){
                                return new AsyncTaskResult<>(e);
                            }
                        }
                        return new AsyncTaskResult<>(did);
                    }else{
                        return new AsyncTaskResult<>(new Exception("Error save Meta ID"));
                    }
                }
            }catch(Exception e){
                return new AsyncTaskResult<>(e);
            }
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<String> result){

            if(result.getError() != null){
                // 생성 실패 에러
                PrintLog.e(result.getError().getMessage());
                PrintLog.e(result.getError().getMessage());
                AlertDialog.Builder builder = new AlertDialog.Builder(activity,R.style.AppCompatAlertDialogStyle);
                builder.setMessage(activity.getString(R.string.error_creation_meta_id));
                builder.setPositiveButton(activity.getString(R.string.retry),
                        (dialog, which) -> {
                            // MetaID 생성 재시도
                            new CreateMetaIdTask(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (String) null);
                            dialog.dismiss();
                        })
                        .setCancelable(false);
                builder.setNegativeButton(activity.getString(R.string.cancel),
                        (dialogInterface, i) -> dialogInterface.dismiss());
                builder.show();
            }else{
                finishListener.finishOK(result.getResult());
                PrintLog.e(result.getResult());
            }
        }
    }

    public void saveDid(Context context, String nickName, String did, BlockChainType type, boolean favorite){
        DidDataVo didDataVo;
        ArrayList<DidDataVo> didList;
        String didListJson = CommonPreference.getInstance(context).getSecureSharedPreferences().getString(DID_LIST, null);
        if(didListJson != null){
            didList = IITPApplication.gson.fromJson(didListJson, new TypeToken<ArrayList<DidDataVo>>(){
            }.getType());
        }else{
            didList = new ArrayList<>();
        }
        KeyManager keyManager = new KeyManager(did);
        long time = System.currentTimeMillis();
        String mnemonic = keyManager.getManagementMnemonic(context);
        String privateKey = Numeric.toHexStringWithPrefix(keyManager.getPrivate(context));
        String publicKey = Numeric.toHexStringWithPrefix(keyManager.getPublicKey(context));
        PrintLog.e("private Key = " + privateKey);
        PrintLog.e("public Key  = " + publicKey);
        PrintLog.e("get ManagementKey = " + mnemonic);
        PrintLog.e("time = " + time);
        PrintLog.e("contract = " + type);
        didDataVo = new DidDataVo(keyManager.getDid(context), privateKey, publicKey, time, nickName, type, favorite, null,mnemonic);
        didList.add(didDataVo);
        MainActivity.setDidList(didList, context);
    }

    /**
     * MetaID 삭제 진행
     */
    static class DeleteMetaIdTask extends AsyncTask<String, Void, AsyncTaskResult<String>>{
        @SuppressLint("StaticFieldLeak")
        private Activity activity;


        DeleteMetaIdTask(Activity activity){
            this.activity = activity;
        }

        @Override
        protected void onPreExecute(){
        }

        @Override
        protected AsyncTaskResult<String> doInBackground(String... strings){
            try{
                // proxy 서버에 public key 를 삭제 요청
                MetaDelegator metaDelegator = new MetaDelegator(false);
                String removePublikeyTx = metaDelegator.removePublicKeyDelegated(activity, strings[0]);
                TransactionReceipt removePkreceipt = Web3jUtils.ethGetTransactionReceipt(metaDelegator.getWeb3j(), removePublikeyTx);

                // proxy 서버에 ServiceKeyResolver 의 자신의 모든 key 를 삭제 요청
                String transactionHash = metaDelegator.removeKeysDelegated(activity, strings[0]);
                TransactionReceipt receipt = Web3jUtils.ethGetTransactionReceipt(metaDelegator.getWeb3j(), transactionHash);

                //delete associated address 추가
                String removeAssociatedAddressTx = metaDelegator.removeAssociatedAddressDelegated(activity, strings[0]);
                TransactionReceipt removeAssociatedreceipt = Web3jUtils.ethGetTransactionReceipt(metaDelegator.getWeb3j(), removeAssociatedAddressTx);

                if(receipt.getStatus().equals("0x1") && removePkreceipt.getStatus().equals("0x1") && removeAssociatedreceipt.getStatus().equals("0x1")){
                    // delete
                    KeyManager keyManager = new KeyManager(strings[0]);
                    keyManager.clear(activity);
//                    KeyManager.getInstance().clear(activity);
                    return new AsyncTaskResult<>(null);
                }else{
                    if(!receipt.getStatus().equals("0x1")){
                        return new AsyncTaskResult<>(new Exception("TransactionReceipt.status is " + receipt.getStatus()));
                    }else if(!removePkreceipt.getStatus().equals("0x1")){
                        return new AsyncTaskResult<>(new Exception("TransactionReceipt.status is " + removePkreceipt.getStatus()));
                    }else{
                        return new AsyncTaskResult<>(new Exception("TransactionReceipt.status is " + removeAssociatedreceipt.getStatus()));
                    }
                }
            }catch(Exception e){
                return new AsyncTaskResult<>(e);
            }
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<String> result){
            if(result.getError() == null){
                // 데이터 삭제하고 초기화면으로 이동
            }else{
            }
        }
    }
}
