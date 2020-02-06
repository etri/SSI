package com.iitp.core.contract;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.2.0.
 */
public class ServiceKeyResolver extends Contract {
    private static final String BINARY = "60806040526201518060045534801561001757600080fd5b50604051602080611b858339810180604052602081101561003757600080fd5b505160008054600160a060020a03909216600160a060020a0319909216919091179055611b1c806100696000396000f3fe6080604052600436106100ae5763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166335434b5d81146100b35780635437b67c1461010d578063600667261461013457806369e78499146101825780637f1ccc25146101b55780638677ebe814610202578063871fc606146102505780638a4ac03f146102dd5780638d357fa314610388578063a36fee1714610402578063c9b2e52214610417575b600080fd5b3480156100bf57600080fd5b5061010b600480360360c08110156100d657600080fd5b50600160a060020a03813581169160208101359091169060ff6040820135169060608101359060808101359060a001356104bf565b005b34801561011957600080fd5b506101226107d4565b60408051918252519081900360200190f35b34801561014057600080fd5b5061010b600480360360a081101561015757600080fd5b50600160a060020a038135169060ff60208201351690604081013590606081013590608001356107da565b34801561018e57600080fd5b5061010b600480360360208110156101a557600080fd5b5035600160a060020a0316610adc565b3480156101c157600080fd5b506101ee600480360360408110156101d857600080fd5b50600160a060020a038135169060200135610b60565b604080519115158252519081900360200190f35b34801561020e57600080fd5b506101ee600480360360a081101561022557600080fd5b50600160a060020a038135169060208101359060ff6040820135169060608101359060800135610c8e565b34801561025c57600080fd5b5061010b6004803603604081101561027357600080fd5b600160a060020a03823516919081019060408101602082013564010000000081111561029e57600080fd5b8201836020820111156102b057600080fd5b803590602001918460018302840111640100000000831117156102d257600080fd5b509092509050610cba565b3480156102e957600080fd5b5061010b600480360360e081101561030057600080fd5b600160a060020a03823581169260208101359091169181019060608101604082013564010000000081111561033457600080fd5b82018360208201111561034657600080fd5b8035906020019184600183028401116401000000008311171561036857600080fd5b919350915060ff8135169060208101359060408101359060600135610d75565b34801561039457600080fd5b506103b2600480360360208110156103ab57600080fd5b50356110e3565b60408051602080825283518183015283519192839290830191858101910280838360005b838110156103ee5781810151838201526020016103d6565b505050509050019250505060405180910390f35b34801561040e57600080fd5b5061010b611265565b34801561042357600080fd5b5061044a6004803603602081101561043a57600080fd5b5035600160a060020a03166112e7565b6040805160208082528351818301528351919283929083019185019080838360005b8381101561048457818101518382015260200161046c565b50505050905090810190601f1680156104b15780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b808042101580156104d35750600454810142105b1515610529576040805160e560020a62461bcd02815260206004820152601760248201527f54696d657374616d70206973206e6f742076616c69642e000000000000000000604482015290519081900360640190fd5b600080546040805160e060020a6305c62c2f028152600160a060020a038b81166004830152915191909216916305c62c2f916024808301926020929190829003018186803b15801561057a57600080fd5b505afa15801561058e573d6000803e3d6000fd5b505050506040513d60208110156105a457600080fd5b5051600054604080517f53a9698a000000000000000000000000000000000000000000000000000000008152600481018490523360248201529051929350600160a060020a03909116916353a9698a91604480820192602092909190829003018186803b15801561061457600080fd5b505afa158015610628573d6000803e3d6000fd5b505050506040513d602081101561063e57600080fd5b50511515610696576040805160e560020a62461bcd02815260206004820152601f60248201527f4f6e6c792070726f76696465722063616e2062652064656c6567617465642e00604482015290519081900360640190fd5b604080517f1900000000000000000000000000000000000000000000000000000000000000602080830191909152600060218301526c0100000000000000000000000030810260228401527f4920617574686f72697a65207468652072656d6f76616c206f6620612073657260368401527f76696365206b6579206f6e206d7920626568616c662e000000000000000000006056840152600160a060020a038b1602606c83015260808083018790528351808403909101815260a0909201909252805191012061076a908990888888610c8e565b15156107c0576040805160e560020a62461bcd02815260206004820152601260248201527f5065726d697373696f6e2064656e6965642e0000000000000000000000000000604482015290519081900360640190fd5b6107ca8188611392565b5050505050505050565b60045481565b808042101580156107ee5750600454810142105b1515610844576040805160e560020a62461bcd02815260206004820152601760248201527f54696d657374616d70206973206e6f742076616c69642e000000000000000000604482015290519081900360640190fd5b600080546040805160e060020a6305c62c2f028152600160a060020a038a81166004830152915191909216916305c62c2f916024808301926020929190829003018186803b15801561089557600080fd5b505afa1580156108a9573d6000803e3d6000fd5b505050506040513d60208110156108bf57600080fd5b5051600054604080517f53a9698a000000000000000000000000000000000000000000000000000000008152600481018490523360248201529051929350600160a060020a03909116916353a9698a91604480820192602092909190829003018186803b15801561092f57600080fd5b505afa158015610943573d6000803e3d6000fd5b505050506040513d602081101561095957600080fd5b505115156109b1576040805160e560020a62461bcd02815260206004820152601f60248201527f4f6e6c792070726f76696465722063616e2062652064656c6567617465642e00604482015290519081900360640190fd5b604080517f1900000000000000000000000000000000000000000000000000000000000000602080830191909152600060218301526c01000000000000000000000000300260228301527f4920617574686f72697a65207468652072656d6f76616c206f6620616c6c207360368301527f657276696365206b657973206f6e206d7920626568616c662e000000000000006056830152606f80830187905283518084039091018152608f9092019092528051910120610a74908890888888610c8e565b1515610aca576040805160e560020a62461bcd02815260206004820152601260248201527f5065726d697373696f6e2064656e6965642e0000000000000000000000000000604482015290519081900360640190fd5b610ad3816114dc565b50505050505050565b6000546040805160e060020a6305c62c2f0281523360048201529051610b5d92600160a060020a0316916305c62c2f916024808301926020929190829003018186803b158015610b2b57600080fd5b505afa158015610b3f573d6000803e3d6000fd5b505050506040513d6020811015610b5557600080fd5b505182611392565b50565b60008054604080517f5b5aed3a0000000000000000000000000000000000000000000000000000000081526004810185905290518492600160a060020a031691635b5aed3a916024808301926020929190829003018186803b158015610bc557600080fd5b505afa158015610bd9573d6000803e3d6000fd5b505050506040513d6020811015610bef57600080fd5b50511515610c6d576040805160e560020a62461bcd02815260206004820152602760248201527f546865207265666572656e636564206964656e7469747920646f6573206e6f7460448201527f2065786973742e00000000000000000000000000000000000000000000000000606482015290519081900360840190fd5b5050600160a060020a03919091166000908152600260205260409020541490565b6000610c9d8686868686611550565b80610cb05750610cb086868686866115d8565b9695505050505050565b6000546040805160e060020a6305c62c2f0281523360048201529051610d7092600160a060020a0316916305c62c2f916024808301926020929190829003018186803b158015610d0957600080fd5b505afa158015610d1d573d6000803e3d6000fd5b505050506040513d6020811015610d3357600080fd5b5051604080516020601f86018190048102820181019092528481528691869086908190840183828082843760009201919091525061169392505050565b505050565b80804210158015610d895750600454810142105b1515610ddf576040805160e560020a62461bcd02815260206004820152601760248201527f54696d657374616d70206973206e6f742076616c69642e000000000000000000604482015290519081900360640190fd5b600080546040805160e060020a6305c62c2f028152600160a060020a038d81166004830152915191909216916305c62c2f916024808301926020929190829003018186803b158015610e3057600080fd5b505afa158015610e44573d6000803e3d6000fd5b505050506040513d6020811015610e5a57600080fd5b5051600054604080517f53a9698a000000000000000000000000000000000000000000000000000000008152600481018490523360248201529051929350600160a060020a03909116916353a9698a91604480820192602092909190829003018186803b158015610eca57600080fd5b505afa158015610ede573d6000803e3d6000fd5b505050506040513d6020811015610ef457600080fd5b50511515610f4c576040805160e560020a62461bcd02815260206004820152601f60248201527f4f6e6c792070726f76696465722063616e2062652064656c6567617465642e00604482015290519081900360640190fd5b6040517f190000000000000000000000000000000000000000000000000000000000000060208201818152600060218401819052306c0100000000000000000000000081810260228701527f4920617574686f72697a6520746865206164646974696f6e206f66206120736560368701527f7276696365206b6579206f6e206d7920626568616c662e0000000000000000006056870152600160a060020a038f1602606d860152611040948f94938f918f918f918c91906081018484808284378083019250505082815260200197505050505050505060405160208183030381529060405280519060200120888888610c8e565b1515611096576040805160e560020a62461bcd02815260206004820152601260248201527f5065726d697373696f6e2064656e6965642e0000000000000000000000000000604482015290519081900360640190fd5b6110d7818a8a8a8080601f01602080910402602001604051908101604052809392919081815260200183838082843760009201919091525061169392505050565b50505050505050505050565b600054604080517f5b5aed3a0000000000000000000000000000000000000000000000000000000081526004810184905290516060928492600160a060020a0390911691635b5aed3a91602480820192602092909190829003018186803b15801561114d57600080fd5b505afa158015611161573d6000803e3d6000fd5b505050506040513d602081101561117757600080fd5b505115156111f5576040805160e560020a62461bcd02815260206004820152602760248201527f546865207265666572656e636564206964656e7469747920646f6573206e6f7460448201527f2065786973742e00000000000000000000000000000000000000000000000000606482015290519081900360840190fd5b600083815260016020908152604091829020805483518184028101840190945280845290929183919083018282801561125757602002820191906000526020600020905b8154600160a060020a03168152600190910190602001808311611239575b505050505092505050919050565b6000546040805160e060020a6305c62c2f02815233600482015290516112e592600160a060020a0316916305c62c2f916024808301926020929190829003018186803b1580156112b457600080fd5b505afa1580156112c8573d6000803e3d6000fd5b505050506040513d60208110156112de57600080fd5b50516114dc565b565b600160a060020a03811660009081526003602090815260409182902080548351601f60026000196101006001861615020190931692909204918201849004840281018401909452808452606093928301828280156113865780601f1061135b57610100808354040283529160200191611386565b820191906000526020600020905b81548152906001019060200180831161136957829003601f168201915b50505050509050919050565b600054604080517fd4b1cdcc0000000000000000000000000000000000000000000000000000000081526004810185905230602482015290518492600160a060020a03169163d4b1cdcc916044808301926020929190829003018186803b1580156113fc57600080fd5b505afa158015611410573d6000803e3d6000fd5b505050506040513d602081101561142657600080fd5b505115156114a4576040805160e560020a62461bcd02815260206004820152603560248201527f5468652063616c6c696e67206964656e7469747920646f6573206e6f7420686160448201527f76652074686973207265736f6c766572207365742e0000000000000000000000606482015290519081900360840190fd5b600160a060020a038216600090815260026020908152604080832083905585835260019091529020610d70908363ffffffff6117fd16565b6000818152600160205260408120905b6114f58261193f565b81101561154057600060026000846000018481548110151561151357fe5b6000918252602080832090910154600160a060020a031683528201929092526040019020556001016114ec565b5061154c816000611a37565b5050565b600085600160a060020a031660018686868660405160008152602001604052604051808581526020018460ff1660ff1681526020018381526020018281526020019450505050506020604051602081039080840390855afa1580156115b9573d6000803e3d6000fd5b50505060206040510351600160a060020a031614905095945050505050565b604080518082018252601c8082527f19457468657265756d205369676e6564204d6573736167653a0a33320000000060208084019182529351600094611688938b9386938c9301918291908083835b602083106116465780518252601f199092019160209182019101611627565b51815160209384036101000a60001901801990921691161790529201938452506040805180850381529382019052825192019190912091508890508787611550565b979650505050505050565b600054604080517fd4b1cdcc0000000000000000000000000000000000000000000000000000000081526004810186905230602482015290518592600160a060020a03169163d4b1cdcc916044808301926020929190829003018186803b1580156116fd57600080fd5b505afa158015611711573d6000803e3d6000fd5b505050506040513d602081101561172757600080fd5b505115156117a5576040805160e560020a62461bcd02815260206004820152603560248201527f5468652063616c6c696e67206964656e7469747920646f6573206e6f7420686160448201527f76652074686973207265736f6c766572207365742e0000000000000000000000606482015290519081900360840190fd5b600160a060020a03831660009081526002602090815260408083208790556003825290912083516117d892850190611a55565b5060008481526001602052604090206117f7908463ffffffff61194316565b50505050565b61180782826119a2565b1561154c578160016118188261193f565b0381548110151561182557fe5b6000918252602080832090910154600160a060020a0384811684526001860190925260409092205484549190921691849160001990910190811061186557fe5b6000918252602080832091909101805473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a03948516179055918316815260018401918290526040812054845490929190859060001985019081106118c257fe5b6000918252602080832090910154600160a060020a039081168452838201949094526040928301822094909455918416825260018501909252908120558154829080151561190c57fe5b6000828152602090208101600019908101805473ffffffffffffffffffffffffffffffffffffffff191690550190555050565b5490565b61194d82826119a2565b151561154c578154600180820180855560008581526020808220909401805473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a03969096169586179055938452930190526040902055565b600160a060020a0381166000908152600183016020526040812054811080156119e65750600160a060020a0382166000908152600184016020526040902054835410155b8015611a305750600160a060020a03821660008181526001850160205260409020548454859160001901908110611a1957fe5b600091825260209091200154600160a060020a0316145b9392505050565b5080546000825590600052602060002090810190610b5d9190611ad3565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10611a9657805160ff1916838001178555611ac3565b82800160010185558215611ac3579182015b82811115611ac3578251825591602001919060010190611aa8565b50611acf929150611ad3565b5090565b611aed91905b80821115611acf5760008155600101611ad9565b9056fea165627a7a723058207cdaa7b57c375b27d3bfb270d7bbc506d12dd4c02187352482134ba024761fff0029";

    public static final String FUNC_REMOVEKEYDELEGATED = "removeKeyDelegated";

    public static final String FUNC_SIGNATURETIMEOUT = "signatureTimeout";

    public static final String FUNC_REMOVEKEYSDELEGATED = "removeKeysDelegated";

    public static final String FUNC_REMOVEKEY = "removeKey";

    public static final String FUNC_ISKEYFOR = "isKeyFor";

    public static final String FUNC_ISSIGNED = "isSigned";

    public static final String FUNC_ADDKEY = "addKey";

    public static final String FUNC_ADDKEYDELEGATED = "addKeyDelegated";

    public static final String FUNC_GETKEYS = "getKeys";

    public static final String FUNC_REMOVEKEYS = "removeKeys";

    public static final String FUNC_GETSYMBOL = "getSymbol";

    public static final Event KEYADDED_EVENT = new Event("KeyAdded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>(true) {}, new TypeReference<Utf8String>() {}));
    ;

    public static final Event KEYREMOVED_EVENT = new Event("KeyRemoved", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>(true) {}));
    ;

    @Deprecated
    protected ServiceKeyResolver(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected ServiceKeyResolver(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected ServiceKeyResolver(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected ServiceKeyResolver(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteCall<TransactionReceipt> removeKeyDelegated(String associatedAddress, String key, BigInteger v, byte[] r, byte[] s, BigInteger timestamp) {
        final Function function = new Function(
                FUNC_REMOVEKEYDELEGATED, 
                Arrays.<Type>asList(new Address(associatedAddress),
                new Address(key),
                new org.web3j.abi.datatypes.generated.Uint8(v), 
                new org.web3j.abi.datatypes.generated.Bytes32(r), 
                new org.web3j.abi.datatypes.generated.Bytes32(s), 
                new Uint256(timestamp)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> signatureTimeout() {
        final Function function = new Function(FUNC_SIGNATURETIMEOUT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> removeKeysDelegated(String associatedAddress, BigInteger v, byte[] r, byte[] s, BigInteger timestamp) {
        final Function function = new Function(
                FUNC_REMOVEKEYSDELEGATED, 
                Arrays.<Type>asList(new Address(associatedAddress),
                new org.web3j.abi.datatypes.generated.Uint8(v), 
                new org.web3j.abi.datatypes.generated.Bytes32(r), 
                new org.web3j.abi.datatypes.generated.Bytes32(s), 
                new Uint256(timestamp)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> removeKey(String key) {
        final Function function = new Function(
                FUNC_REMOVEKEY, 
                Arrays.<Type>asList(new Address(key)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Boolean> isKeyFor(String key, BigInteger ein) {
        final Function function = new Function(FUNC_ISKEYFOR, 
                Arrays.<Type>asList(new Address(key),
                new Uint256(ein)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<Boolean> isSigned(String _address, byte[] messageHash, BigInteger v, byte[] r, byte[] s) {
        final Function function = new Function(FUNC_ISSIGNED, 
                Arrays.<Type>asList(new Address(_address),
                new org.web3j.abi.datatypes.generated.Bytes32(messageHash), 
                new org.web3j.abi.datatypes.generated.Uint8(v), 
                new org.web3j.abi.datatypes.generated.Bytes32(r), 
                new org.web3j.abi.datatypes.generated.Bytes32(s)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<TransactionReceipt> addKey(String key, String symbol) {
        final Function function = new Function(
                FUNC_ADDKEY, 
                Arrays.<Type>asList(new Address(key),
                new Utf8String(symbol)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> addKeyDelegated(String associatedAddress, String key, String symbol, BigInteger v, byte[] r, byte[] s, BigInteger timestamp) {
        final Function function = new Function(
                FUNC_ADDKEYDELEGATED, 
                Arrays.<Type>asList(new Address(associatedAddress),
                new Address(key),
                new Utf8String(symbol),
                new org.web3j.abi.datatypes.generated.Uint8(v), 
                new org.web3j.abi.datatypes.generated.Bytes32(r), 
                new org.web3j.abi.datatypes.generated.Bytes32(s), 
                new Uint256(timestamp)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<List> getKeys(BigInteger ein) {
        final Function function = new Function(FUNC_GETKEYS, 
                Arrays.<Type>asList(new Uint256(ein)),
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Address>>() {}));
        return new RemoteCall<List>(
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteCall<TransactionReceipt> removeKeys() {
        final Function function = new Function(
                FUNC_REMOVEKEYS, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> getSymbol(String key) {
        final Function function = new Function(FUNC_GETSYMBOL, 
                Arrays.<Type>asList(new Address(key)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public List<KeyAddedEventResponse> getKeyAddedEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(KEYADDED_EVENT, transactionReceipt);
        ArrayList<KeyAddedEventResponse> responses = new ArrayList<KeyAddedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            KeyAddedEventResponse typedResponse = new KeyAddedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.key = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.ein = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.symbol = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<KeyAddedEventResponse> keyAddedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, KeyAddedEventResponse>() {
            @Override
            public KeyAddedEventResponse apply(Log log) {
                EventValuesWithLog eventValues = extractEventParametersWithLog(KEYADDED_EVENT, log);
                KeyAddedEventResponse typedResponse = new KeyAddedEventResponse();
                typedResponse.log = log;
                typedResponse.key = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.ein = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.symbol = (String) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<KeyAddedEventResponse> keyAddedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(KEYADDED_EVENT));
        return keyAddedEventFlowable(filter);
    }

    public List<KeyRemovedEventResponse> getKeyRemovedEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(KEYREMOVED_EVENT, transactionReceipt);
        ArrayList<KeyRemovedEventResponse> responses = new ArrayList<KeyRemovedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            KeyRemovedEventResponse typedResponse = new KeyRemovedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.key = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.ein = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<KeyRemovedEventResponse> keyRemovedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, KeyRemovedEventResponse>() {
            @Override
            public KeyRemovedEventResponse apply(Log log) {
                EventValuesWithLog eventValues = extractEventParametersWithLog(KEYREMOVED_EVENT, log);
                KeyRemovedEventResponse typedResponse = new KeyRemovedEventResponse();
                typedResponse.log = log;
                typedResponse.key = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.ein = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<KeyRemovedEventResponse> keyRemovedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(KEYREMOVED_EVENT));
        return keyRemovedEventFlowable(filter);
    }

    @Deprecated
    public static ServiceKeyResolver load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new ServiceKeyResolver(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static ServiceKeyResolver load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new ServiceKeyResolver(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static ServiceKeyResolver load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new ServiceKeyResolver(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static ServiceKeyResolver load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new ServiceKeyResolver(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<ServiceKeyResolver> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, String identityRegistryAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(identityRegistryAddress)));
        return deployRemoteCall(ServiceKeyResolver.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<ServiceKeyResolver> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, String identityRegistryAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(identityRegistryAddress)));
        return deployRemoteCall(ServiceKeyResolver.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<ServiceKeyResolver> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String identityRegistryAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(identityRegistryAddress)));
        return deployRemoteCall(ServiceKeyResolver.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<ServiceKeyResolver> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String identityRegistryAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(identityRegistryAddress)));
        return deployRemoteCall(ServiceKeyResolver.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static class KeyAddedEventResponse {
        public Log log;

        public String key;

        public BigInteger ein;

        public String symbol;
    }

    public static class KeyRemovedEventResponse {
        public Log log;

        public String key;

        public BigInteger ein;
    }
}
