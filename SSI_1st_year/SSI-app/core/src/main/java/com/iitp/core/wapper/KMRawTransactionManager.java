package com.iitp.core.wapper;

import android.content.Context;

import com.iitp.core.crypto.KeyManager;

import org.web3j.crypto.RawTransaction;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Transaction manager. sign through {@link KeyManager}.<br>
 * Not need PrivateLKey ({@link org.web3j.crypto.Credentials})
 * See {@link KeyManager#signTransaction(Context, String, RawTransaction)} <br>
 */
public class KMRawTransactionManager extends TransactionManager {
    private Web3j web3j;
    private Context context;
    private Nonce nonce;

    /**
     * construct
     * @see KMRawTransactionManager#KMRawTransactionManager(Web3j, Context, String, TransactionReceiptProcessor, Nonce)
     */
    public KMRawTransactionManager(Web3j web3j, Context context, String address, Nonce nonce) {
        this(web3j, context, address, (TransactionReceiptProcessor)(new PollingTransactionReceiptProcessor(web3j, 1000L, 400)), nonce);
    }

    /**
     * construct
     * @see KMRawTransactionManager#KMRawTransactionManager(Web3j, Context, String, TransactionReceiptProcessor, Nonce)
     */
    public KMRawTransactionManager(Web3j web3j, Context context, String address) {
        this(web3j, context, address, (TransactionReceiptProcessor)(new PollingTransactionReceiptProcessor(web3j, 1000L, 400)), null);
    }

    /**
     * construct for TransactionReceiptProcessor
     * @param web3j                       web3j
     * @param context                     Android context for KeyManager
     * @param address                     address of private key for KeyManager
     * @param transactionReceiptProcessor transaction receipt processor
     */
    public KMRawTransactionManager(Web3j web3j, Context context, String address, TransactionReceiptProcessor transactionReceiptProcessor, Nonce nonce) {
        super(transactionReceiptProcessor, address);
        this.context = context;
        this.web3j = web3j;
        if (nonce == null) {
            this.nonce = new DefaultNonce(web3j, address);
        }
        else {
            this.nonce = nonce;
        }
    }

    /**
     * Get nonce
     * @return transaction count
     * @throws IOException network error
     */
    private BigInteger getNonce() throws IOException {
        return nonce.getNonce();
    }

    @Override
    public EthSendTransaction sendTransaction(BigInteger gasPrice, BigInteger gasLimit, String to, String data, BigInteger value) throws IOException {
        BigInteger nonce = this.getNonce();
        if (gasPrice == null) {
            gasPrice = web3j.ethGasPrice().send().getGasPrice();
        }
        if (gasLimit == null) {
            gasLimit = web3j.ethEstimateGas(new Transaction(getFromAddress(), nonce, gasPrice, BigInteger.ZERO, to, value, data)).send().getAmountUsed();
        }
        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, value, data);

        return this.signAndSend(rawTransaction);
    }

    /**
     * sign and send transaction
     * @param rawTransaction raw transaction
     * @return result of send transaction
     * @throws IOException network error
     */
    private EthSendTransaction signAndSend(RawTransaction rawTransaction) throws IOException {
        String signedTransaction = KeyManager.getInstance().signTransaction(context, getFromAddress(), rawTransaction);
        return (EthSendTransaction) this.web3j.ethSendRawTransaction(signedTransaction).send();
    }
}
