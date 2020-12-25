package com.iitp.core.contract;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicBytes;
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
 * <p>Generated with web3j version 4.3.0.
 */
public class PublicKeyResolver extends Contract {
    private static final String BINARY = "60806040526201518060025534801561001757600080fd5b506040516020806114ae8339810180604052602081101561003757600080fd5b505160008054600160a060020a03909216600160a060020a0319909216919091179055611445806100696000396000f3fe6080604052600436106100985763ffffffff7c0100000000000000000000000000000000000000000000000000000000600035041663173c5249811461009d5780633c31b7ea146100ed5780634e21a5171461018f5780635437b67c1461020c578063857cdbb8146102335780638677ebe8146102db578063a061bc741461033d578063a3f4df7e14610352578063e8a4c04e14610367575b600080fd5b3480156100a957600080fd5b506100eb600480360360a08110156100c057600080fd5b50600160a060020a038135169060ff6020820135169060408101359060608101359060800135610436565b005b3480156100f957600080fd5b506100eb600480360360c081101561011057600080fd5b600160a060020a03823516919081019060408101602082013564010000000081111561013b57600080fd5b82018360208201111561014d57600080fd5b8035906020019184600183028401116401000000008311171561016f57600080fd5b919350915060ff813516906020810135906040810135906060013561074c565b34801561019b57600080fd5b506100eb600480360360208110156101b257600080fd5b8101906020810181356401000000008111156101cd57600080fd5b8201836020820111156101df57600080fd5b8035906020019184600183028401116401000000008311171561020157600080fd5b509092509050610abc565b34801561021857600080fd5b50610221610b76565b60408051918252519081900360200190f35b34801561023f57600080fd5b506102666004803603602081101561025657600080fd5b5035600160a060020a0316610b7c565b6040805160208082528351818301528351919283929083019185019080838360005b838110156102a0578181015183820152602001610288565b50505050905090810190601f1680156102cd5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b3480156102e757600080fd5b50610329600480360360a08110156102fe57600080fd5b50600160a060020a038135169060208101359060ff6040820135169060608101359060800135610c26565b604080519115158252519081900360200190f35b34801561034957600080fd5b506100eb610c52565b34801561035e57600080fd5b50610266610cd7565b34801561037357600080fd5b5061041a6004803603602081101561038a57600080fd5b8101906020810181356401000000008111156103a557600080fd5b8201836020820111156103b757600080fd5b803590602001918460018302840111640100000000831117156103d957600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600092019190915250929550610d0e945050505050565b60408051600160a060020a039092168252519081900360200190f35b8080421015801561044a5750600254810142105b15156104a0576040805160e560020a62461bcd02815260206004820152601760248201527f54696d657374616d70206973206e6f742076616c69642e000000000000000000604482015290519081900360640190fd5b600080546040805160e060020a6305c62c2f028152600160a060020a038a81166004830152915191909216916305c62c2f916024808301926020929190829003018186803b1580156104f157600080fd5b505afa158015610505573d6000803e3d6000fd5b505050506040513d602081101561051b57600080fd5b5051600054604080517f53a9698a000000000000000000000000000000000000000000000000000000008152600481018490523360248201529051929350600160a060020a03909116916353a9698a91604480820192602092909190829003018186803b15801561058b57600080fd5b505afa15801561059f573d6000803e3d6000fd5b505050506040513d60208110156105b557600080fd5b5051151561060d576040805160e560020a62461bcd02815260206004820152601f60248201527f4f6e6c792070726f76696465722063616e2062652064656c6567617465642e00604482015290519081900360640190fd5b604080517f1900000000000000000000000000000000000000000000000000000000000000602080830191909152600060218301526c0100000000000000000000000030810260228401527f4920617574686f72697a65207468652072656d6f76616c206f6620612070756260368401527f6c6963206b6579206f6e206d7920626568616c662e00000000000000000000006056840152600160a060020a038b1602606b830152607f80830187905283518084039091018152609f90920190925280519101206106e1908890888888610c26565b1515610737576040805160e560020a62461bcd02815260206004820152601260248201527f5065726d697373696f6e2064656e6965642e0000000000000000000000000000604482015290519081900360640190fd5b61074381886001610d1e565b50505050505050565b808042101580156107605750600254810142105b15156107b6576040805160e560020a62461bcd02815260206004820152601760248201527f54696d657374616d70206973206e6f742076616c69642e000000000000000000604482015290519081900360640190fd5b600080546040805160e060020a6305c62c2f028152600160a060020a038c81166004830152915191909216916305c62c2f916024808301926020929190829003018186803b15801561080757600080fd5b505afa15801561081b573d6000803e3d6000fd5b505050506040513d602081101561083157600080fd5b5051600054604080517f53a9698a000000000000000000000000000000000000000000000000000000008152600481018490523360248201529051929350600160a060020a03909116916353a9698a91604480820192602092909190829003018186803b1580156108a157600080fd5b505afa1580156108b5573d6000803e3d6000fd5b505050506040513d60208110156108cb57600080fd5b50511515610923576040805160e560020a62461bcd02815260206004820152601f60248201527f4f6e6c792070726f76696465722063616e2062652064656c6567617465642e00604482015290519081900360640190fd5b6040517f190000000000000000000000000000000000000000000000000000000000000060208201818152600060218401819052306c0100000000000000000000000081810260228701527f4920617574686f72697a6520746865206164646974696f6e206f66206120707560368701527f626c6963206b6579206f6e206d7920626568616c662e000000000000000000006056870152600160a060020a038f1602606c860152610a17948e949385918f918f918c91906080018484808284378083019250505082815260200197505050505050505060405160208183030381529060405280519060200120888888610c26565b1515610a6d576040805160e560020a62461bcd02815260206004820152601260248201527f5065726d697373696f6e2064656e6965642e0000000000000000000000000000604482015290519081900360640190fd5b610ab1818a8a8a8080601f01602080910402602001604051908101604052809392919081815260200183838082843760009201919091525060019250610e99915050565b505050505050505050565b6000546040805160e060020a6305c62c2f0281523360048201529051610b7292600160a060020a0316916305c62c2f916024808301926020929190829003018186803b158015610b0b57600080fd5b505afa158015610b1f573d6000803e3d6000fd5b505050506040513d6020811015610b3557600080fd5b5051604080516020601f86018190048102820181019092528481523391869086908190840183828082843760009201829052509250610e99915050565b5050565b60025481565b600160a060020a03811660009081526001602081815260409283902080548451600294821615610100026000190190911693909304601f81018390048302840183019094528383526060939091830182828015610c1a5780601f10610bef57610100808354040283529160200191610c1a565b820191906000526020600020905b815481529060010190602001808311610bfd57829003601f168201915b50505050509050919050565b6000610c3586868686866111f4565b80610c485750610c48868686868661127c565b9695505050505050565b6000546040805160e060020a6305c62c2f0281523360048201529051610cd592600160a060020a0316916305c62c2f916024808301926020929190829003018186803b158015610ca157600080fd5b505afa158015610cb5573d6000803e3d6000fd5b505050506040513d6020811015610ccb57600080fd5b5051336000610d1e565b565b60408051808201909152601181527f5075626c69634b65795265736f6c766572000000000000000000000000000000602082015281565b8051602090910120600081905290565b600054604080517fd4b1cdcc0000000000000000000000000000000000000000000000000000000081526004810186905230602482015290518592600160a060020a03169163d4b1cdcc916044808301926020929190829003018186803b158015610d8857600080fd5b505afa158015610d9c573d6000803e3d6000fd5b505050506040513d6020811015610db257600080fd5b50511515610e30576040805160e560020a62461bcd02815260206004820152603560248201527f5468652063616c6c696e67206964656e7469747920646f6573206e6f7420686160448201527f76652074686973207265736f6c766572207365742e0000000000000000000000606482015290519081900360840190fd5b600160a060020a0383166000908152600160205260408120610e5191611337565b60408051831515815290518591600160a060020a038616917f627d6da185122bce6a368e7e5c48234968d76011fc1277894aa208786d8ece1e9181900360200190a350505050565b600054604080517fd4b1cdcc0000000000000000000000000000000000000000000000000000000081526004810187905230602482015290518692600160a060020a03169163d4b1cdcc916044808301926020929190829003018186803b158015610f0357600080fd5b505afa158015610f17573d6000803e3d6000fd5b505050506040513d6020811015610f2d57600080fd5b50511515610fab576040805160e560020a62461bcd02815260206004820152603560248201527f5468652063616c6c696e67206964656e7469747920646f6573206e6f7420686160448201527f76652074686973207265736f6c766572207365742e0000000000000000000000606482015290519081900360840190fd5b838381600160a060020a0316610fc082610d0e565b600160a060020a03161461106a576040805160e560020a62461bcd02815260206004820152604260248201527f5468652061646472657373206973206e6f74207468652073616d65206173207460448201527f68617420636f6e7665727465642066726f6d20746865207075626c6963206b6560648201527f792e000000000000000000000000000000000000000000000000000000000000608482015290519081900360a40190fd5b600160a060020a0386166000908152600160208190526040909120546002918116156101000260001901160415611111576040805160e560020a62461bcd02815260206004820152602160248201527f4b65792077617320616c726561647920616464656420627920736f6d656f6e6560448201527f2e00000000000000000000000000000000000000000000000000000000000000606482015290519081900360840190fd5b600160a060020a0386166000908152600160209081526040909120865161113a9288019061137e565b508686600160a060020a03167fc604da33cee952ded8ce0db2b489092b367cc77da1b4965a9b76e48455b4797c8787604051808060200183151515158152602001828103825284818151815260200191508051906020019080838360005b838110156111b0578181015183820152602001611198565b50505050905090810190601f1680156111dd5780820380516001836020036101000a031916815260200191505b50935050505060405180910390a350505050505050565b600085600160a060020a031660018686868660405160008152602001604052604051808581526020018460ff1660ff1681526020018381526020018281526020019450505050506020604051602081039080840390855afa15801561125d573d6000803e3d6000fd5b50505060206040510351600160a060020a031614905095945050505050565b604080518082018252601c8082527f19457468657265756d205369676e6564204d6573736167653a0a3332000000006020808401918252935160009461132c938b9386938c9301918291908083835b602083106112ea5780518252601f1990920191602091820191016112cb565b51815160209384036101000a600019018019909216911617905292019384525060408051808503815293820190528251920191909120915088905087876111f4565b979650505050505050565b50805460018160011615610100020316600290046000825580601f1061135d575061137b565b601f01602090049060005260206000209081019061137b91906113fc565b50565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106113bf57805160ff19168380011785556113ec565b828001600101855582156113ec579182015b828111156113ec5782518255916020019190600101906113d1565b506113f89291506113fc565b5090565b61141691905b808211156113f85760008155600101611402565b9056fea165627a7a723058209fa1a0f6f83f1c7808aa3b225c1ef27e96bbce10419fbd7f22a75ab1b54b6c800029\n";

    public static final String FUNC_SIGNATURETIMEOUT = "signatureTimeout";

    public static final String FUNC_ISSIGNED = "isSigned";

    public static final String FUNC_NAME = "NAME";

    public static final String FUNC_CALCULATEADDRESS = "calculateAddress";

    public static final String FUNC_ADDPUBLICKEYDELEGATED = "addPublicKeyDelegated";

    public static final String FUNC_ADDPUBLICKEY = "addPublicKey";

    public static final String FUNC_REMOVEPUBLICKEYDELEGATED = "removePublicKeyDelegated";

    public static final String FUNC_REMOVEPUBLICKEY = "removePublicKey";

    public static final String FUNC_GETPUBLICKEY = "getPublicKey";

    public static final Event PUBLICKEYADDED_EVENT = new Event("PublicKeyAdded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>(true) {}, new TypeReference<DynamicBytes>() {}, new TypeReference<Bool>() {}));
    ;

    public static final Event PUBLICKEYREMOVED_EVENT = new Event("PublicKeyRemoved", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>(true) {}, new TypeReference<Bool>() {}));
    ;

    @Deprecated
    protected PublicKeyResolver(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected PublicKeyResolver(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected PublicKeyResolver(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected PublicKeyResolver(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteCall<BigInteger> signatureTimeout() {
        final Function function = new Function(FUNC_SIGNATURETIMEOUT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<Boolean> isSigned(String _address, byte[] messageHash, BigInteger v, byte[] r, byte[] s) {
        final Function function = new Function(FUNC_ISSIGNED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_address), 
                new org.web3j.abi.datatypes.generated.Bytes32(messageHash), 
                new org.web3j.abi.datatypes.generated.Uint8(v), 
                new org.web3j.abi.datatypes.generated.Bytes32(r), 
                new org.web3j.abi.datatypes.generated.Bytes32(s)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<String> NAME() {
        final Function function = new Function(FUNC_NAME, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public List<PublicKeyAddedEventResponse> getPublicKeyAddedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(PUBLICKEYADDED_EVENT, transactionReceipt);
        ArrayList<PublicKeyAddedEventResponse> responses = new ArrayList<PublicKeyAddedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PublicKeyAddedEventResponse typedResponse = new PublicKeyAddedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.addr = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.ein = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.publicKey = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.delegated = (Boolean) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<PublicKeyAddedEventResponse> publicKeyAddedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, PublicKeyAddedEventResponse>() {
            @Override
            public PublicKeyAddedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(PUBLICKEYADDED_EVENT, log);
                PublicKeyAddedEventResponse typedResponse = new PublicKeyAddedEventResponse();
                typedResponse.log = log;
                typedResponse.addr = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.ein = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.publicKey = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.delegated = (Boolean) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<PublicKeyAddedEventResponse> publicKeyAddedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PUBLICKEYADDED_EVENT));
        return publicKeyAddedEventFlowable(filter);
    }

    public List<PublicKeyRemovedEventResponse> getPublicKeyRemovedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(PUBLICKEYREMOVED_EVENT, transactionReceipt);
        ArrayList<PublicKeyRemovedEventResponse> responses = new ArrayList<PublicKeyRemovedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PublicKeyRemovedEventResponse typedResponse = new PublicKeyRemovedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.addr = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.ein = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.delegated = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<PublicKeyRemovedEventResponse> publicKeyRemovedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, PublicKeyRemovedEventResponse>() {
            @Override
            public PublicKeyRemovedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(PUBLICKEYREMOVED_EVENT, log);
                PublicKeyRemovedEventResponse typedResponse = new PublicKeyRemovedEventResponse();
                typedResponse.log = log;
                typedResponse.addr = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.ein = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.delegated = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<PublicKeyRemovedEventResponse> publicKeyRemovedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PUBLICKEYREMOVED_EVENT));
        return publicKeyRemovedEventFlowable(filter);
    }

    public RemoteCall<String> calculateAddress(byte[] publicKey) {
        final Function function = new Function(FUNC_CALCULATEADDRESS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicBytes(publicKey)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> addPublicKeyDelegated(String associatedAddress, byte[] publicKey, BigInteger v, byte[] r, byte[] s, BigInteger timestamp) {
        final Function function = new Function(
                FUNC_ADDPUBLICKEYDELEGATED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(associatedAddress), 
                new org.web3j.abi.datatypes.DynamicBytes(publicKey), 
                new org.web3j.abi.datatypes.generated.Uint8(v), 
                new org.web3j.abi.datatypes.generated.Bytes32(r), 
                new org.web3j.abi.datatypes.generated.Bytes32(s), 
                new org.web3j.abi.datatypes.generated.Uint256(timestamp)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> addPublicKey(byte[] publicKey) {
        final Function function = new Function(
                FUNC_ADDPUBLICKEY, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicBytes(publicKey)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> removePublicKeyDelegated(String associatedAddress, BigInteger v, byte[] r, byte[] s, BigInteger timestamp) {
        final Function function = new Function(
                FUNC_REMOVEPUBLICKEYDELEGATED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(associatedAddress), 
                new org.web3j.abi.datatypes.generated.Uint8(v), 
                new org.web3j.abi.datatypes.generated.Bytes32(r), 
                new org.web3j.abi.datatypes.generated.Bytes32(s), 
                new org.web3j.abi.datatypes.generated.Uint256(timestamp)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> removePublicKey() {
        final Function function = new Function(
                FUNC_REMOVEPUBLICKEY, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<byte[]> getPublicKey(String addr) {
        final Function function = new Function(FUNC_GETPUBLICKEY, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(addr)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    @Deprecated
    public static PublicKeyResolver load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new PublicKeyResolver(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static PublicKeyResolver load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new PublicKeyResolver(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static PublicKeyResolver load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new PublicKeyResolver(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static PublicKeyResolver load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new PublicKeyResolver(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<PublicKeyResolver> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, String identityRegistryAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(identityRegistryAddress)));
        return deployRemoteCall(PublicKeyResolver.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<PublicKeyResolver> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, String identityRegistryAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(identityRegistryAddress)));
        return deployRemoteCall(PublicKeyResolver.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<PublicKeyResolver> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String identityRegistryAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(identityRegistryAddress)));
        return deployRemoteCall(PublicKeyResolver.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<PublicKeyResolver> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String identityRegistryAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(identityRegistryAddress)));
        return deployRemoteCall(PublicKeyResolver.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static class PublicKeyAddedEventResponse {
        public Log log;

        public String addr;

        public BigInteger ein;

        public byte[] publicKey;

        public Boolean delegated;
    }

    public static class PublicKeyRemovedEventResponse {
        public Log log;

        public String addr;

        public BigInteger ein;

        public Boolean delegated;
    }
}
