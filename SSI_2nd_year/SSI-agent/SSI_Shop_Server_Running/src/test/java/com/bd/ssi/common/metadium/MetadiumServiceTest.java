package com.bd.ssi.common.metadium;

import com.bd.ssi.common.fcm.FcmService;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.VerifiablePresentation;
import com.iitp.verifiable.signer.MetadiumSigner;
import com.iitp.verifiable.util.ECKeyUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigInteger;
import java.security.interfaces.ECPrivateKey;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class MetadiumServiceTest {
    @Autowired
    MetadiumService metadiumService;

    @Test
    public void test() throws Exception {

        String ISSUER_DID = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001bd1";
        String ISSUER_KID = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001bd1#MetaManagementKey#bae67d929f5758cceb8e43cdc6056eff7bbe4f92";
        ECPrivateKey ISSUER_PRIVATE_KEY = ECKeyUtils.toECPrivateKey(new BigInteger("5deae036a001840b80817cea7d8f0af503206f2749008c7ea3406395a480c8a0", 16), "secp256k1"); // PrivateKey load

        String USER_DID = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001bd4";
        String USER_KID = "did:meta:testnet:0000000000000000000000000000000000000000000000000000000000001bd4#MetaManagementKey#f57c6a7655d4325dd10d55ab1854e4708eef5423";
        ECPrivateKey USER_PRIVATE_KEY = ECKeyUtils.toECPrivateKey(new BigInteger("16e9f408b4ef19c4fa7ff45b05fed96581ac938c6e05fc3695065197e36f4f18", 16), "secp256k1"); // PrivateKey load

        // 만료일
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 2);

        // VC 발급 - Issuer 가 발급
        VerifiableCredential vc = new VerifiableCredential();
        vc.setTypes(Arrays.asList("CREDENTIAL", "IdentificationCredential"));
        vc.setExpirationDate(calendar.getTime());
        vc.setIssuanceDate(new Date());
        Map<String, Object> subject = new HashMap<>();
        subject.put("id", USER_DID);	// 소유자 DID. 반드시 넣어야 함
        subject.put("name", "Gil-dong Hong");
        subject.put("birth_date", "1988-07-21");
        subject.put("address", "218 Gajeong-ro, Yuseong-gu, Daejeon, 34129, KOREA");
        vc.setCredentialSubject(subject);
        String signedVc = new MetadiumSigner(ISSUER_DID, ISSUER_KID, ISSUER_PRIVATE_KEY).sign(vc); // issuer 의 DID 로 서명

// VP 생성 - 사용자가 VP 생성
        VerifiablePresentation vp = new VerifiablePresentation();
        vp.setTypes(Arrays.asList("PRESENTATION", "MyPresentation"));
        vp.addVerifiableCredential(signedVc);
        String signedVp = new MetadiumSigner(USER_DID, USER_KID, USER_PRIVATE_KEY).sign(vp); // 사용자의 DID 로 서명

        VerifiablePresentation resultVp = metadiumService.verifyVp(signedVp);
        for (Object vcObject : resultVp.getVerifiableCredentials()) {
            if (vcObject instanceof String) {
                VerifiableCredential resultVc = metadiumService.verifyVc(signedVc);

                ArrayList<String> types = resultVc.getTypes().stream().collect(Collectors.toCollection(ArrayList::new));
                System.out.println(types.get(1));

                Map<String, Object> claims = (Map<String, Object>)resultVc.getCredentialSubject();
                System.out.println(claims.toString());
            }
        }
    }
}