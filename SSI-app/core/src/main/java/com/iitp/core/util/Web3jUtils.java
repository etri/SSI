package com.iitp.core.util;


import android.util.Log;

import com.iitp.util.StringUtils;
import com.iitp.util.backoff.BackOffExecution;
import com.iitp.util.backoff.ExponentialBackOff;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;

/**
 * Web3j utility
 */
public class Web3jUtils {
    public static DecimalFormat getDefaultDecimalFormat() {
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setGroupingUsed(true);
        decimalFormat.setMaximumFractionDigits(18);
        decimalFormat.setParseBigDecimal(true);
        return decimalFormat;
    }

    /**
     * convert wei to ether string format<br>
     * 1001230000000000000000 => 1,001.23
     * @param integer wei
     * @return ether format string
     */
    public static String wei2ethString(BigInteger integer) {
        BigDecimal ether = Convert.fromWei(new BigDecimal(integer), Convert.Unit.ETHER);
        return getDefaultDecimalFormat().format(ether);
    }

    /**
     * conver wei to string<br/>
     * 1,001,230,000,000,000,000,000
     * @param integer wei
     * @return format string
     */
    public static String wei2String(BigInteger integer) {
        return getDefaultDecimalFormat().format(integer);
    }

    /**
     * convert ether to wei
     * @param ether ether
     * @return wei
     */
    public static BigInteger ether2Wei(BigDecimal ether) {
        return Convert.toWei(ether, Convert.Unit.ETHER).toBigInteger();
    }

    /**
     * convert wei to ether
     * @param wei wei
     * @return ether
     */
    public static BigDecimal wei2ether(BigInteger wei) {
        return Convert.fromWei(new BigDecimal(wei), Convert.Unit.ETHER);
    }

    /**
     * GetTransactionReceipt with ExponentialBackOff
     * @param web3j           web3
     * @param transactionHash tx hash
     * @return transaction receipt
     * @throws IOException io error or timeout
     */
    public static TransactionReceipt ethGetTransactionReceipt(Web3j web3j, String transactionHash) throws IOException {
        // request transaction receipt to API (GETH) for use exponential back off
        EthGetTransactionReceipt transactionReceipt;
        ExponentialBackOff exponentialBackOff = new ExponentialBackOff(500, 1.2);
        exponentialBackOff.setMaxElapsedTime(30000);
        exponentialBackOff.setMaxInterval(5000);
        BackOffExecution execution = exponentialBackOff.start();
        do {
            long backOff = execution.nextBackOff();
            if (backOff != BackOffExecution.STOP) {
                try {
                    Thread.sleep(backOff);
                } catch (InterruptedException e) {
                    // no effect
                    Log.e("Web3jUtils", "exception error ");
                }
            }
            else {
                throw new IOException("timeout");
            }

            transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send();
        }
        while (!transactionReceipt.hasError() && transactionReceipt.getTransactionReceipt().isEmpty());

        return transactionReceipt.getTransactionReceipt().get();
    }

    /**
     * check valid ether address.
     * @param address ether address
     * @return valid is true
     */
    public static boolean isValidAddress(String address) {
        if (StringUtils.empty(address)) {
            return false;
        }

        if (!Numeric.containsHexPrefix(address)) {
            return false;
        }

        String noPrefixAddress = Numeric.cleanHexPrefix(address);
        if (noPrefixAddress.length() != 40) {
            return false;
        }

        try {
            Numeric.toBigInt(address);
        }
        catch (Exception e) {
            // not valid
            return false;
        }
        return true;
    }

    /**
     * check valid private key
     * @param privateKey hex-string private key
     * @return valid is true
     */
    public static boolean isValidPrivateKey(String privateKey) {
        if (StringUtils.empty(privateKey)) {
            return false;
        }

        String noPrefixPrivateKey = Numeric.cleanHexPrefix(privateKey);
        if (noPrefixPrivateKey.length() != 64) {
            return false;
        }

        try {
            Numeric.toBigInt(noPrefixPrivateKey);
        }
        catch (Exception e) {
            return false;
        }

        return true;
    }
}
