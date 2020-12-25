package com.iitp.core.contract;

import com.iitp.core.protocol.data.RegistryAddress;
import com.iitp.core.wapper.ZeroContractGasProvider;
import com.iitp.util.StringUtils;

import org.web3j.protocol.Web3j;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.List;

/**
 * IdentityRegister contract helper
 */
public class IdentityRegistryHelper {
    /**
     * associatedAddress 해당하는 Identity 에 resolverAddress 를 반환한다.<br>
     * resolverAddress 는 RegistryAddress 의 serviceKeyAll 에 반드시 포함된 address 임.
     * @param web3j             web3
     * @param registryAddress   address 정보
     * @param associatedAddress resolverAddress 를 확인할 assoicatedAddress
     * @return ServiceKeyResolver 의 address
     * @throws Exception resolver address 가 없거나 ethCall 에러
     */
    public static String getServiceKeyResolverAddressOfIdentity(Web3j web3j, RegistryAddress registryAddress, String associatedAddress) throws Exception, RuntimeException {
        IdentityRegistry identityRegistry = IdentityRegistry.load(
                registryAddress.identityRegistry,
                web3j,
                new ReadonlyTransactionManager(web3j, null),
                new ZeroContractGasProvider()
        );
        BigInteger ein = identityRegistry.getEIN(associatedAddress).send();
        return getServiceKeyResolverAddressOfIdentity(web3j, identityRegistry, registryAddress, ein);
    }

    /**
     * 해당 Identity 의 Metadium의 ServiceKeyResolver 의 address 를 반환한다.<br/>
     * @param web3j            web3
     * @param identityRegistry IdentityRegistry contract object
     * @param registryAddress  address 정보
     * @param ein              Meta ID
     * @return ServiceKeyResolver contract 의 address
     * @throws Exception address 가 없음
     */
    public static String getServiceKeyResolverAddressOfIdentity(Web3j web3j, IdentityRegistry identityRegistry, RegistryAddress registryAddress, BigInteger ein) throws RuntimeException{
        Tuple4<String, List<String>, List<String>, List<String>> identity = null;
        try{
            identity = identityRegistry.getIdentity(ein).send();
        }catch(Exception e){
            throw new RuntimeException("Get Identity error");
        }
        List<String> resolverAddressOfIdentity = identity.getValue4();
        String resolverAddress = null;
        for (String address : resolverAddressOfIdentity) {
            if (registryAddress.serviceKeyAll.indexOf(address) >= 0) {
                resolverAddress = address;
                break;
            }
        }
        if (resolverAddress == null) {
            throw new RuntimeException("Not exists address of ServiceKeyResolver in registry");
        }
        return resolverAddress;
    }

    /**
     * 해당 Identity 의 public key의 유무를 반환<br/>
     * @param identityRegistry      IdentityRegistry contract object
     * @param registryAddress       address 정보
     * @param ein                   Meta ID
     * @return  true/false
     * @throws Exception            error
     */
    public static boolean getCheckPublicKeyHas(Web3j web3j,IdentityRegistry identityRegistry, RegistryAddress registryAddress, BigInteger ein, String associatedAddress, String publicKey) throws Exception {
        boolean hasPublicKey = false;
        Tuple4<String, List<String>, List<String>, List<String>> identity = identityRegistry.getIdentity(ein).send();
        List<String> publicKeyResolverList = registryAddress.publicKeyAll;

        if (identity.getValue4().size() != 0) {
            List<String> resolverList = identity.getValue4();
            for (String publicKeyAddress : publicKeyResolverList) {
                for (String resolverAddress : resolverList) {
                    if (StringUtils.equals(publicKeyAddress, resolverAddress)) {
                        PublicKeyResolver pkResolver = PublicKeyResolver.load(resolverAddress,
                                web3j,
                                new ReadonlyTransactionManager(web3j, null),
                                new ZeroContractGasProvider());
                        String pkAddress = Numeric.toHexString(pkResolver.getPublicKey(associatedAddress).send()) ;
                        if(publicKey.equals(pkAddress)){
                            hasPublicKey = true;
                            break;
                        }

                    }
                }
//                if (hasPublicKey) {
//                    break;
//                }
            }
        }
        return hasPublicKey;
    }
}
