package com.iitp.core.wapper;

import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;

/**
 * Gas Price, Limit 가 0 인 Provider<br/>
 * {@link org.web3j.tx.gas.StaticGasProvider} 사용을 해도 됩니다.
 */
public class ZeroContractGasProvider implements ContractGasProvider {
    @Override
    public BigInteger getGasPrice(String contractFunc) {
        return BigInteger.ZERO;
    }

    @Override
    public BigInteger getGasLimit(String contractFunc) {
        return BigInteger.ZERO;
    }

    @Override
    public BigInteger getGasPrice() {
        return BigInteger.ZERO;
    }

    @Override
    public BigInteger getGasLimit() {
        return BigInteger.ZERO;
    }
}
