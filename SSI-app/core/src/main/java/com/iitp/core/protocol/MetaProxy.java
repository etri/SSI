package com.iitp.core.protocol;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iitp.core.contract.IdentityRegistry;
import com.iitp.core.contract.IdentityRegistryHelper;
import com.iitp.core.crypto.KeyManager;
import com.iitp.core.protocol.data.RegistryAddress;
import com.iitp.core.util.NumericUtils;
import com.iitp.core.wapper.NotSignTransactionManager;
import com.iitp.core.wapper.ZeroContractGasProvider;
import com.iitp.util.Bytes;

import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Client of Metadium Proxy server<br>
 * default test-net url is http://52.79.240.215:8545<br/>
 * https://drive.google.com/open?id=1p5sOaJVfuelJ8ifgk4-De3zL0trnRXQo
 */
public class MetaProxy{
    /**
     * default testnet url
     */
    private static final String PROXY_URL = "https://15.164.64.229:8545";
//    private static final String PROXY_URL = "https://testdelegator.metadium.com:8545";

    private static final String METHOD_GET_ALL_SERVICE_ADDRESSES = "get_all_service_addresses";

    private HttpService httpService;

    public MetaProxy(String url){
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // log
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger(){
            @Override
            public void log(@NonNull String message){
                Log.d("MetaProxy", message);
            }
        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(loggingInterceptor);

        httpService = new HttpService(url == null ? PROXY_URL : url, builder.build(), false);
    }

    /**
     * Get System registry address
     *
     * @return registry address
     */
    @SuppressWarnings("unchecked")
    public RegistryAddress getAllServiceAddress() throws IOException, JSONRPCException{
        Response<Map> response = new Request(METHOD_GET_ALL_SERVICE_ADDRESSES, null, httpService, Response.class).send();
        if(response.getError() == null){
            return new ObjectMapper().convertValue(response.getResult(), RegistryAddress.class);
        }else{
            throw new JSONRPCException(response.getError());
        }
    }


    /**
     * Get timestamp of node
     *
     * @param web3j web
     * @return epoch timestamp
     */
    private long getTimestamp(Web3j web3j){
        if(web3j != null){
            try{
                return web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send().getBlock().getTimestamp().longValue();
            }catch(IOException e){
                // return system timestamp
                Log.e("MetaProxy","exception error");
                return System.currentTimeMillis() / 1000;
            }
        }
        return System.currentTimeMillis() / 1000;
    }

    /**
     * Create meta id<br/>
     * 각 parameter 항목에 대해서는 IdentityRegistry.createIdentityDelegated 함수를 참고 하세요.<br/>
     * https://github.com/METADIUM/MetaResolvers/blob/master/contracts/IdentityRegistry.sol
     *
     * @param context           android context
     * @param web3j             web3
     * @param keyPair           서명에 사용할 key pair
     * @param registryAddress   address 정보. {@link #getAllServiceAddress()}
     * @param recoveryAddress   recovery address
     * @param associatedAddress associated address
     * @return transaction has
     * @throws IOException      io error
     * @throws JSONRPCException json-rpc error
     */
    @SuppressWarnings("unchecked")
    public String createIdentityDelegated(Context context, Web3j web3j, ECKeyPair keyPair, RegistryAddress registryAddress, String recoveryAddress, String associatedAddress) throws IOException, JSONRPCException{
        long timestamp = getTimestamp(web3j);
        byte[] message = Bytes.concat(
                new byte[]{0x19, 0x00},
                Numeric.hexStringToByteArray(registryAddress.identityRegistry),
                "I authorize the creation of an Identity on my behalf.".getBytes(),
                Numeric.hexStringToByteArray(recoveryAddress),
                Numeric.hexStringToByteArray(associatedAddress),
                NumericUtils.hexStringArrayToByteArray(registryAddress.providers.toArray(new String[registryAddress.providers.size()]), 32),
                NumericUtils.hexStringArrayToByteArray(registryAddress.resolvers.toArray(new String[registryAddress.resolvers.size()]), 32),
                Numeric.toBytesPadded(BigInteger.valueOf(timestamp), 32)
        );
        Sign.SignatureData signatureData;
        if(keyPair == null){
            signatureData = KeyManager.stringToSignatureData(KeyManager.getInstance().signMessage(context, associatedAddress, message));
        }else{
            signatureData = Sign.signMessage(message, keyPair);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("recovery_address", recoveryAddress);
        params.put("associated_address", associatedAddress);
        params.put("providers", registryAddress.providers);
        params.put("resolvers", registryAddress.resolvers);
        params.put("timestamp", timestamp);
        params.put("v", Numeric.toHexString(new byte[]{signatureData.getV()}));
        params.put("r", Numeric.toHexString(signatureData.getR()));
        params.put("s", Numeric.toHexString(signatureData.getS()));

        Response<String> response = new Request("create_identity", Collections.singletonList(params), httpService, Response.class).send();
        if(response.getError() == null){
            return response.getResult();
        }else{
            throw new JSONRPCException(response.getError());
        }
    }

    /**
     * {@link com.coinplug.metadium.core.contract.ServiceKeyResolver} 에 key 를 추가
     * https://github.com/METADIUM/MetaResolvers/blob/master/contracts/examples/Resolvers/ServiceKey/ServiceKeyResolver.sol 에서 addKeyDelegated 함수 참고
     *
     * @param context           android context
     * @param web3j             web3
     * @param registryAddress   address 정보. {@link #getAllServiceAddress()}
     * @param associatedAddress 추가할 key 의 소유자 address
     * @param key               추가할 key
     * @param symbol            service id
     * @return transaction hash
     * @throws Exception transaction error
     */
    @SuppressWarnings("unchecked")
    public String addKeyDelegated(Context context, Web3j web3j, RegistryAddress registryAddress, String associatedAddress, String key, String symbol) throws Exception,RuntimeException,JSONRPCException{
        String resolverAddress = IdentityRegistryHelper.getServiceKeyResolverAddressOfIdentity(web3j, registryAddress, associatedAddress);

        long timestamp = getTimestamp(web3j);
        byte[] message = Bytes.concat(
                new byte[]{0x19, 0x00},
                Numeric.hexStringToByteArray(resolverAddress),
                "I authorize the addition of a service key on my behalf.".getBytes(),
                Numeric.hexStringToByteArray(key),
                symbol.getBytes(),
                Numeric.toBytesPadded(BigInteger.valueOf(timestamp), 32)
        );
        Sign.SignatureData signatureData = KeyManager.stringToSignatureData(KeyManager.getInstance().signMessage(context, associatedAddress, message));

        Map<String, Object> params = new HashMap<>();
        params.put("resolver_address", resolverAddress);
        params.put("associated_address", associatedAddress);
        params.put("key", key);
        params.put("symbol", symbol);
        params.put("timestamp", timestamp);
        params.put("v", Numeric.toHexString(new byte[]{signatureData.getV()}));
        params.put("r", Numeric.toHexString(signatureData.getR()));
        params.put("s", Numeric.toHexString(signatureData.getS()));

        Response<String> response = new Request("add_key_delegated", Collections.singletonList(params), httpService, Response.class).send();
        if(response.getError() == null){
            return response.getResult();
        }else{
            throw new JSONRPCException(response.getError());
        }
    }

    /**
     * {@link com.coinplug.metadium.core.contract.ServiceKeyResolver} 에 key 를 삭제
     * https://github.com/METADIUM/MetaResolvers/blob/master/contracts/examples/Resolvers/ServiceKey/ServiceKeyResolver.sol 에서 removeKeyDelegated 함수 참고
     *
     * @param context           android context
     * @param web3j             web3
     * @param registryAddress   address 정보. {@link #getAllServiceAddress()}
     * @param associatedAddress 삭제할 key 의 소유자 address
     * @param key               삭제할 key
     * @return transaction hash
     * @throws Exception transaction error
     */
    @SuppressWarnings("unchecked")
    public String removeKeyDelegated(Context context, Web3j web3j, RegistryAddress registryAddress, String associatedAddress, String key) throws Exception,RuntimeException,JSONRPCException{
        String resolverAddress = IdentityRegistryHelper.getServiceKeyResolverAddressOfIdentity(web3j, registryAddress, associatedAddress);

        long timestamp = getTimestamp(web3j);
        byte[] message = Bytes.concat(
                new byte[]{0x19, 0x00},
                Numeric.hexStringToByteArray(resolverAddress),
                "I authorize the removal of a service key on my behalf.".getBytes(),
                Numeric.hexStringToByteArray(key),
                Numeric.toBytesPadded(BigInteger.valueOf(timestamp), 32)
        );
        Sign.SignatureData signatureData = KeyManager.stringToSignatureData(KeyManager.getInstance().signMessage(context, associatedAddress, message));

        Map<String, Object> params = new HashMap<>();
        params.put("resolver_address", resolverAddress);
        params.put("associated_address", associatedAddress);
        params.put("key", key);
        params.put("timestamp", timestamp);
        params.put("v", Numeric.toHexString(new byte[]{signatureData.getV()}));
        params.put("r", Numeric.toHexString(signatureData.getR()));
        params.put("s", Numeric.toHexString(signatureData.getS()));


        Response<String> response = new Request("remove_key_delegated", Collections.singletonList(params), httpService, Response.class).send();
        if(response.getError() == null){
            return response.getResult();
        }else{
            throw new JSONRPCException(response.getError());
        }
    }

    /**
     * {@link com.coinplug.metadium.core.contract.ServiceKeyResolver} 의 모든 key 를 삭제한다.
     * https://github.com/METADIUM/MetaResolvers/blob/master/contracts/examples/Resolvers/ServiceKey/ServiceKeyResolver.sol 에서 removeKeysDelegated 함수 참고
     *
     * @param context           android context
     * @param web3j             web3
     * @param registryAddress   address 정보. {@link #getAllServiceAddress()}
     * @param associatedAddress 소유자 address
     * @return transaction hash
     * @throws Exception transaction error
     */
    @SuppressWarnings("unchecked")
    public String removeKeysDelegated(Context context, Web3j web3j, RegistryAddress registryAddress, String associatedAddress) throws Exception,RuntimeException, JSONRPCException{
        String resolverAddress = IdentityRegistryHelper.getServiceKeyResolverAddressOfIdentity(web3j, registryAddress, associatedAddress);

        long timestamp = getTimestamp(web3j);
        byte[] message = Bytes.concat(
                new byte[]{0x19, 0x00},
                Numeric.hexStringToByteArray(resolverAddress),
                "I authorize the removal of all service keys on my behalf.".getBytes(),
                Numeric.toBytesPadded(BigInteger.valueOf(timestamp), 32)
        );
        Sign.SignatureData signatureData = KeyManager.stringToSignatureData(KeyManager.getInstance().signMessage(context, associatedAddress, message));

        Map<String, Object> params = new HashMap<>();
        params.put("resolver_address", resolverAddress);
        params.put("associated_address", associatedAddress);
        params.put("timestamp", timestamp);
        params.put("v", Numeric.toHexString(new byte[]{signatureData.getV()}));
        params.put("r", Numeric.toHexString(signatureData.getR()));
        params.put("s", Numeric.toHexString(signatureData.getS()));


        Response<String> response = new Request("remove_keys_delegated", Collections.singletonList(params), httpService, Response.class).send();
        if(response.getError() == null){
            return response.getResult();
        }else{
            throw new JSONRPCException(response.getError());
        }
    }

    /**
     * Add publicKey <br/>
     * https://github.com/METADIUM/MetaResolvers/blob/master/contracts/examples/Resolvers/PublicKey/PublicKeyResolver.sol
     *
     * @param context           android context
     * @param web3j             web3j
     * @param registryAddress   address 정보. {@link #getAllServiceAddress()}
     * @param associatedAddress 소유자 address
     * @return transaction hash
     * @throws IOException      io error
     * @throws JSONRPCException json rpc error
     */
    @SuppressWarnings("unchecked")
    public String addPublicKeyDelegated(Context context, Web3j web3j, ECKeyPair keyPair, RegistryAddress registryAddress, String associatedAddress) throws IOException, JSONRPCException{
        String resolverAddress = registryAddress.publicKey;
        long timestamp = getTimestamp(web3j);
        String publicKey = "0x" + Numeric.toHexStringNoPrefixZeroPadded(keyPair.getPublicKey(), 128);
        byte[] message = Bytes.concat(
                new byte[]{0x19, 0x00},
                Numeric.hexStringToByteArray(resolverAddress),
                "I authorize the addition of a public key on my behalf.".getBytes(),
                Numeric.hexStringToByteArray(associatedAddress),
                Numeric.hexStringToByteArray(publicKey),
                Numeric.toBytesPadded(BigInteger.valueOf(timestamp), 32)
        );

        Sign.SignatureData signatureData = KeyManager.stringToSignatureData(KeyManager.getInstance().signMessage(context, associatedAddress, message));

        Map<String, Object> params = new HashMap<>();
        params.put("resolver_address", resolverAddress);
        params.put("associated_address", associatedAddress);
        params.put("public_key", publicKey);
        params.put("timestamp", timestamp);
        params.put("v", Numeric.toHexString(new byte[]{signatureData.getV()}));
        params.put("r", Numeric.toHexString(signatureData.getR()));
        params.put("s", Numeric.toHexString(signatureData.getS()));


        Response<String> response = new Request("add_public_key_delegated", Collections.singletonList(params), httpService, Response.class).send();
        if(response.getError() == null){
            return response.getResult();
        }else{
            throw new JSONRPCException(response.getError());
        }
    }

    /**
     * Delete publicKey <br/>
     * https://github.com/METADIUM/MetaResolvers/blob/master/contracts/examples/Resolvers/PublicKey/PublicKeyResolver.sol
     *
     * @param context           android context
     * @param web3j             web3j
     * @param registryAddress   address 정보. {@link #getAllServiceAddress()}
     * @param associatedAddress 소유자 address
     * @return transaction hash
     * @throws IOException      io error
     * @throws JSONRPCException json rpc error
     */
    @SuppressWarnings("unchecked")
    public String removePublicKeyDelegated(Context context, Web3j web3j, RegistryAddress registryAddress, String associatedAddress) throws IOException, JSONRPCException{
        String resolverAddress = registryAddress.publicKey;
        long timestamp = getTimestamp(web3j);
        byte[] message = Bytes.concat(
                new byte[]{0x19, 0x00},
                Numeric.hexStringToByteArray(resolverAddress),
                "I authorize the removal of a public key on my behalf.".getBytes(),
                Numeric.hexStringToByteArray(associatedAddress),
                Numeric.toBytesPadded(BigInteger.valueOf(timestamp), 32)
        );

        Sign.SignatureData signatureData = KeyManager.stringToSignatureData(KeyManager.getInstance().signMessage(context, associatedAddress, message));

        Map<String, Object> params = new HashMap<>();
        params.put("resolver_address", resolverAddress);
        params.put("associated_address", associatedAddress);
        params.put("timestamp", timestamp);
        params.put("v", Numeric.toHexString(new byte[]{signatureData.getV()}));
        params.put("r", Numeric.toHexString(signatureData.getR()));
        params.put("s", Numeric.toHexString(signatureData.getS()));


        Response<String> response = new Request("remove_public_key_delegated", Collections.singletonList(params), httpService, Response.class).send();
        if(response.getError() == null){
            return response.getResult();
        }else{
            throw new JSONRPCException(response.getError());
        }
    }


    /**
     * Delete Associated Address <br/>
     * https://github.com/METADIUM/MetaResolvers/blob/master/contracts/IdentityRegistry.sol
     *
     * @param context           android context
     * @param web3j             web3j
     * @param registryAddress   address 정보. {@link #getAllServiceAddress()}
     * @param associatedAddress 소유자 address
     * @return transaction hash
     * @throws Exception io error
     */
    @SuppressWarnings("unchecked")
    public String removeAssociatedAddressDelegated(Context context, Web3j web3j, RegistryAddress registryAddress, String associatedAddress) throws Exception,JSONRPCException{
        String identityRegistryAddress = registryAddress.identityRegistry;
        IdentityRegistry identityRegistry = IdentityRegistry.load(registryAddress.identityRegistry, web3j, new NotSignTransactionManager(web3j), new ZeroContractGasProvider());
        BigInteger ein = identityRegistry.getEIN(associatedAddress).send();
        long timestamp = getTimestamp(web3j);
        byte[] message = Bytes.concat(
                new byte[]{0x19, 0x00},
                Numeric.hexStringToByteArray(identityRegistryAddress),
                "I authorize removing this address from my Identity.".getBytes(),
                Numeric.toBytesPadded(ein, 32),
                Numeric.hexStringToByteArray(associatedAddress),
                Numeric.toBytesPadded(BigInteger.valueOf(timestamp), 32)
        );

        Sign.SignatureData signatureData = KeyManager.stringToSignatureData(KeyManager.getInstance().signMessage(context, associatedAddress, message));

        Map<String, Object> params = new HashMap<>();
        params.put("address_to_remove", associatedAddress);
        params.put("timestamp", timestamp);
        params.put("v", Numeric.toHexString(new byte[]{signatureData.getV()}));
        params.put("r", Numeric.toHexString(signatureData.getR()));
        params.put("s", Numeric.toHexString(signatureData.getS()));

        Response<String> response = new Request("remove_associated_address_delegated", Collections.singletonList(params), httpService, Response.class).send();
        if(response.getError() == null){
            return response.getResult();
        }else{
            throw new JSONRPCException(response.getError());
        }
    }

}
