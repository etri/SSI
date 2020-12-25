package com.iitp.iitp_demo.chain;

import com.iitp.iitp_demo.util.PrintLog;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.VerifiableVerifier;
import com.iitp.verifiable.verifier.IconVerifier;
import com.iitp.verifiable.verifier.MetadiumVerifier;

import java.util.ArrayList;
import java.util.List;

public class VCVPVerifier{
    private static VCVPVerifier instance;

    private VCVPVerifier(){
// 한번만 미리 설정
        VerifiableVerifier.register("did:meta:", MetadiumVerifier.class);    // META
        VerifiableVerifier.register("did:icon:", IconVerifier.class);        // ICON
//        VerifiableVerifier.setResolverUrl("https://testnetresolver.metadium.com"); // Set universal resolver (http://129.254.194.103:9000). 테스트로 META resolver
        VerifiableVerifier.setResolverUrl("http://129.254.194.103:9000"); // Set universal resolver (http://129.254.194.103:9000). 테스트로 META resolver
    }

    public static synchronized VCVPVerifier getInstance(){
        if(instance == null){
            instance = new VCVPVerifier();
        }
        return instance;
    }

    public List<VerifiableCredential> verifyVCList(List<String> vcList){
        List<VerifiableCredential> credentialList = new ArrayList<>();
        for (Object vcObject : vcList) {
            if (vcObject instanceof String) {
                VerifiableCredential resultVc = null;
                try{
                    VerifiableVerifier verifiableVerifier = new VerifiableVerifier();
                    resultVc = (VerifiableCredential)verifiableVerifier.verify((String)vcObject);
                    credentialList.add(resultVc);
                }catch(Exception e){
                    PrintLog.e("verifyVCList error");
                }
            }
        }
        return credentialList;
    }

    public VerifiableCredential verifyVC(String vcData){
                VerifiableCredential resultVc = null;
                try{
                    VerifiableVerifier verifiableVerifier = new VerifiableVerifier();
                    resultVc = (VerifiableCredential)verifiableVerifier.verify(vcData);

                }catch(Exception e){
                    PrintLog.e("verifyVC error");
                }
        return resultVc;
    }

//    public void verifyVC(String signedVp ){
//// VP 검증
//        VerifiableVerifier verifiableVerifier = new VerifiableVerifier();
//        VerifiablePresentation resultVp = null;
//        try{
//            resultVp = (VerifiablePresentation)verifiableVerifier.verify(signedVp);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        verifyVp(resultVp);
//    }
//
//    public void verifyVp(VerifiablePresentation resultVp){
//        VerifiableVerifier verifiableVerifier = new VerifiableVerifier();
//        for (Object vcObject : resultVp.getVerifiableCredentials()) {
//            if (vcObject instanceof String) {
//                VerifiableCredential resultVc = null;
//                try{
//                    resultVc = (VerifiableCredential)verifiableVerifier.verify((String)vcObject);
//                }catch(Exception e){
//                    e.printStackTrace();
//                }
//
//                Map<String, Object> claims = (Map<String, Object>)resultVc.getCredentialSubject();
//            }
//        }
//    }


}
