package com.iitp.iconloop.iitp.securities.icon;

import foundation.icon.icx.data.Address;
import foundation.icon.icx.transport.jsonrpc.RpcObject;

/**
 * Score 를 call/transaction 하기 위한 Data
 */
public class ScoreCall {

    private Address address;
    private String method;
    private RpcObject params;
    private boolean readOnly = false;

    public String getMethod() {
        return method;
    }

    public RpcObject getParams() {
        return params;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public Address getAddress() {
        return address;
    }

    public static final class Builder {
        private Address address;
        private String method;
        private RpcObject params;
        private boolean readOnly = false;

        public Builder() {
        }

        public Builder address(Address address) {
            this.address = address;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder params(RpcObject params) {
            this.params = params;
            return this;
        }

        public Builder readOnly(boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }

        public ScoreCall build() {
            ScoreCall scoreCall = new ScoreCall();
            scoreCall.address = this.address;
            scoreCall.params = this.params;
            scoreCall.method = this.method;
            scoreCall.readOnly = this.readOnly;
            return scoreCall;
        }
    }

}
