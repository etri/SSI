package com.iitp.core.wapper;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Eth Get Transaction count
 */
public class DefaultNonce implements Nonce {
    private Web3j web3j;
    private String address;

    public DefaultNonce(Web3j web3j, String address) {
        this.web3j = web3j;
        this.address = address;
    }

    @Override
    public BigInteger getNonce() throws IOException {
        EthGetTransactionCount ethGetTransactionCount = (EthGetTransactionCount) this.web3j
                .ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).send();
        return ethGetTransactionCount.getTransactionCount();
    }
}
