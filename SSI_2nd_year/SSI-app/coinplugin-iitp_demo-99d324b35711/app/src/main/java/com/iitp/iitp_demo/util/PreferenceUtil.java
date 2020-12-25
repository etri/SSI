package com.iitp.iitp_demo.util;

import android.content.Context;
import android.util.Base64;

import com.google.gson.reflect.TypeToken;
import com.iitp.iitp_demo.IITPApplication;
import com.iitp.iitp_demo.activity.model.IndyCredentialVo;
import com.iitp.iitp_demo.activity.model.ProductVC;
import com.iitp.iitp_demo.chain.indy.Indy;

import java.util.ArrayList;
import java.util.List;

import static com.iitp.iitp_demo.Constants.DELEGATOR_TOKEN_SET;
import static com.iitp.iitp_demo.Constants.DID_LIST;
import static com.iitp.iitp_demo.chain.VCVPCreater.DELEGATOR_VC;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreCardTokenKorea;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreCardTokenSeoul;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreDelegationToken;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreIdCard;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreLogin;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreMobile;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStorePost;
import static com.iitp.iitp_demo.chain.VCVPCreater.NoneZKPStoreStock;
import static com.iitp.iitp_demo.chain.VCVPCreater.OfficeCredential;
import static com.iitp.iitp_demo.chain.VCVPCreater.ProductCredential;
import static com.iitp.iitp_demo.chain.VCVPCreater.ProductProofCredential;
import static com.iitp.iitp_demo.chain.VCVPCreater.ZKP_IdentificationCredential;
import static com.iitp.iitp_demo.chain.VCVPCreater.ZKP_UniCredential;

public class PreferenceUtil{

    private static PreferenceUtil _preferenceUtil = null;
    private Context mContext;
    private PreferenceUtil(Context context){
        mContext = context;
    }
    public static PreferenceUtil getInstance(Context ctx){
        if(_preferenceUtil == null){
            _preferenceUtil = new PreferenceUtil(ctx);
        }
        return _preferenceUtil;
    }

    /**
     * 모든 VC가져오기
     *
     * @return vc list
     */
    public List<String> getAllVCData(){
        List<String> vcList = new ArrayList<>();
        String vc = null;
        vc = CommonPreference.getInstance(mContext).getSecureSharedPreferences().getString(NoneZKPStoreCardTokenKorea, null);
        if(vc != null){
            vcList.add(vc);
        }
        vc = CommonPreference.getInstance(mContext).getSecureSharedPreferences().getString(NoneZKPStoreCardTokenSeoul, null);
        if(vc != null){
            vcList.add(vc);
        }
        vc = CommonPreference.getInstance(mContext).getSecureSharedPreferences().getString(NoneZKPStoreIdCard, null);
        if(vc != null){
            vcList.add(vc);
        }
        vc = CommonPreference.getInstance(mContext).getSecureSharedPreferences().getString(NoneZKPStoreMobile, null);
        if(vc != null){
            vcList.add(vc);
        }
        vc = CommonPreference.getInstance(mContext).getSecureSharedPreferences().getString(ProductCredential, null);
        if(vc != null){
            vcList.add(vc);
        }

        vc = CommonPreference.getInstance(mContext).getSecureSharedPreferences().getString(NoneZKPStoreStock, null);
        if(vc != null){
            vcList.add(vc);
        }
        vc = CommonPreference.getInstance(mContext).getSecureSharedPreferences().getString(NoneZKPStorePost, null);
        if(vc != null){
            vcList.add(vc);
        }
        vc = CommonPreference.getInstance(mContext).getSecureSharedPreferences().getString(NoneZKPStoreDelegationToken, null);
        if(vc != null){
            vcList.add(vc);
        }
        vc = CommonPreference.getInstance(mContext).getSecureSharedPreferences().getString(NoneZKPStoreLogin, null);
        if(vc != null){
            vcList.add(vc);
        }
        vc = CommonPreference.getInstance(mContext).getSecureSharedPreferences().getString(DELEGATOR_VC, null);
        if(vc != null){
            vcList.add(vc);
        }
        vc = CommonPreference.getInstance(mContext).getSecureSharedPreferences().getString(ProductProofCredential, null);
        if(vc != null){
            vcList.add(vc);
        }
        vc = CommonPreference.getInstance(mContext).getSecureSharedPreferences().getString(OfficeCredential, null);
        if(vc != null){
            vcList.add(vc);
        }
//        vc = CommonPreference.getInstance(context).getSecureSharedPreferences().getString(ZKP_IdentificationCredential, null);
//        if(vc != null){
//            vcList.add(vc);
//        }
//        vc = CommonPreference.getInstance(context).getSecureSharedPreferences().getString(ZKP_UniCredential, null);
//        if(vc != null){
//            vcList.add(vc);
//        }
        return vcList;
    }

    /**
     * get payload
     *
     * @param jwt jwt
     * @return payload
     */
    public String getPayload(String jwt){
        String payload = null;
        String[] jwtData = jwt.split("\\.");
        if(jwtData[1] != null){
            payload = new String(Base64.decode(jwtData[1], Base64.URL_SAFE));
        }
        return payload;
    }


    public void setDelegatorVC(String jwt){
        List<String> jwtList = new ArrayList<>();
        String jwtData = CommonPreference.getInstance(mContext).getSecureSharedPreferences().getString(DELEGATOR_VC, null);
        if(jwtData != null){
            jwtList = IITPApplication.gson.fromJson(jwtData, new TypeToken<List<String>>(){
            }.getType());
            jwtList.add(jwt);
        }else{
            jwtList.add(jwt);
        }
        String setData = IITPApplication.gson.toJson(jwtList);
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().putString(DELEGATOR_VC, setData).commit();
    }

    public String getDelegatorVC(){
        String jwtData = CommonPreference.getInstance(mContext).getSecureSharedPreferences().getString(DELEGATOR_VC, null);
        return jwtData;
    }

    public static List<String> getDelegatorVCList(String vcList){
        List<String> jwtList = new ArrayList<>();
        jwtList = IITPApplication.gson.fromJson(vcList, new TypeToken<List<String>>(){
        }.getType());
        return jwtList;
    }

    public void setIndyDID(String did){
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().putString("indyDID", did).commit();
    }

    public void removeIndyDID(){
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove("indyDID").apply();
    }

    public String getIndyDID(){
        String did = CommonPreference.getInstance(mContext).getSecureSharedPreferences().getString("indyDID", null);
        return did;
    }

    public void setMetaDID(String did){
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().putString("metaDID", did).commit();
    }

    public String getMetaDID(){
        String did = CommonPreference.getInstance(mContext).getSecureSharedPreferences().getString("metaDID", null);
        return did;
    }

    public ArrayList<ProductVC> getProductVC(){
        ArrayList<ProductVC> productVC;
        String vc = CommonPreference.getInstance(mContext).getSecureSharedPreferences().getString("productVC", null);
//        PrintLog.e("getProductVC=  " + vc);
        productVC = IITPApplication.gson.fromJson(vc, new TypeToken<ArrayList<ProductVC>>(){
        }.getType());
        return productVC;
    }

    public void setProductVC(ProductVC vc){
        ArrayList<ProductVC> productList = getProductVC();
        if(productList != null){
            PrintLog.e("productList size=  = "+productList.size());
        }
        if(productList == null){
            productList = new ArrayList<>();
            productList.add(vc);
        }else{
            productList.add(vc);
        }
        PrintLog.e("productList size=  = "+productList.size());
        String saveData = IITPApplication.gson.toJson(productList);
        PrintLog.e("saveData = "+saveData);
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().putString("productVC", saveData).commit();
    }

    public void removeProductVC(ProductVC vc){
        ArrayList<ProductVC> productList = getProductVC();
        if(productList != null){
            PrintLog.e("productList size=  = "+productList.size());
        }
        if(productList != null){
            productList.remove(vc);
            PrintLog.e("productList size=  = "+productList.size());
        }
        String saveData = IITPApplication.gson.toJson(productList);
        PrintLog.e("saveData = "+saveData);
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().putString("productVC", saveData).commit();
    }


    public boolean  checkIdCredential(){
        boolean checkData = false;
       String vc = CommonPreference.getInstance(mContext).getSecureSharedPreferences().getString(NoneZKPStoreIdCard, null);
       if(vc != null){
           checkData = true;
       }
       return  checkData;
    }

    public boolean  checkProductCredential(){
        boolean checkData = false;
        ArrayList<ProductVC> productVC = null;
        productVC = getProductVC();
        if(productVC != null){
            checkData = true;
        }
        return  checkData;
    }

    public void setWalletJson(String productDID, String walletJson){
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().putString(productDID, walletJson).commit();
    }

    public String getWalletJson(String productDID){
        String walletJson = CommonPreference.getInstance(mContext).getSecureSharedPreferences().getString(productDID, null);
        return walletJson;
    }

    public void setPincode(String pincode){
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().putString("pincode", pincode).commit();
    }

    public String getPincode(){
        return CommonPreference.getInstance(mContext).getSecureSharedPreferences().getString("pincode", null);
    }


    /**
     * reset Data
     */
    public void resetData(){
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove(DID_LIST).apply();
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove(DELEGATOR_TOKEN_SET).apply();
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove(DELEGATOR_VC).apply();
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove(NoneZKPStoreCardTokenKorea).apply();
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove(NoneZKPStoreCardTokenSeoul).apply();
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove(NoneZKPStoreIdCard).apply();
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove(NoneZKPStoreMobile).apply();
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove(ProductCredential).apply();
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove(ProductProofCredential).apply();
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove(NoneZKPStoreStock).apply();
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove(NoneZKPStorePost).apply();
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove(NoneZKPStoreDelegationToken).apply();
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove(NoneZKPStoreLogin).apply();
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove(ZKP_IdentificationCredential).apply();
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove(ZKP_UniCredential).apply();
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove("UniCredentialRequest").apply();
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove("JobCredentialRequest").apply();
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove("IdCredentialRequest").apply();
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove("productVC").apply();
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove("metaDID").apply();
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove("indyDID").apply();
        CommonPreference.getInstance(mContext).getSecureSharedPreferences().edit().remove(OfficeCredential).apply();
        deleteIndy();


    }

    private void deleteIndy(){
        Indy indy  = Indy.getInstance(mContext);
        ArrayList<IndyCredentialVo> indyCredentialVos = null;
        String indyData;
        try{
            indyData = indy.getCredentialsWorksForEmptyFilter();
            PrintLog.e("indyData = " + indyData);
            indyCredentialVos = IITPApplication.gson.fromJson(indyData, new TypeToken<ArrayList<IndyCredentialVo>>(){
            }.getType());
            int size = indyCredentialVos.size();
            PrintLog.e("size = " + size);

        }catch(Exception e){
            PrintLog.e("deleteIndy error");
        }
        if(indyCredentialVos != null){
            for(IndyCredentialVo temp :indyCredentialVos){

                indy.deleteCredential( temp.getReferent());
            }
        }
        try{
            indy.closeWallet();
        }catch(Exception e){
            PrintLog.e("deleteIndy error");
        }
    }
}
