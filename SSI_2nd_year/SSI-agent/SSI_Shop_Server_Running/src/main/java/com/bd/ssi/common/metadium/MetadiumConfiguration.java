package com.bd.ssi.common.metadium;

import com.iitp.verifiable.VerifiableVerifier;
import com.iitp.verifiable.verifier.IconVerifier;
import com.iitp.verifiable.verifier.MetadiumVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetadiumConfiguration {

    @Bean
    public VerifiableVerifier verifiableVerifier(){
        // 한번만 미리 설정
        VerifiableVerifier.register("did:meta:", MetadiumVerifier.class);	// META
        VerifiableVerifier.register("did:icon:", IconVerifier.class);		// ICON
        VerifiableVerifier.setResolverUrl(MetadiumService.TESTNET_URL); // Set universal resolver (http://129.254.194.103:9000). 테스트로 META resolver

        return new VerifiableVerifier();
    }

}
