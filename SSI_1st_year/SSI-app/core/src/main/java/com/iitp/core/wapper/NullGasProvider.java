package com.iitp.core.wapper;

import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;

public class NullGasProvider implements ContractGasProvider {
    @Override
    public BigInteger getGasPrice(String contractFunc) {
        return null;
    }

    @Override
    public BigInteger getGasPrice() {
        return null;
    }

    @Override
    public BigInteger getGasLimit(String contractFunc) {
        return null;
    }

    @Override
    public BigInteger getGasLimit() {
        return null;
    }
}
