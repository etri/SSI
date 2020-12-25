package com.iitp.core.wapper;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.TransactionManager;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Non-sign transaction manager.<br>
 * only getting
 */
public class NotSignTransactionManager extends TransactionManager {
    public NotSignTransactionManager(Web3j web3j) {
        super(web3j, null);
    }

    @Override
    public EthSendTransaction sendTransaction(BigInteger gasPrice, BigInteger gasLimit, String to, String data, BigInteger value) throws IOException {
        return null;
    }
}
