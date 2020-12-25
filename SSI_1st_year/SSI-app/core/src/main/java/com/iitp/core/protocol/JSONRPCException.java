package com.iitp.core.protocol;

import org.web3j.protocol.core.Response;

/**
 * General web3j exception
 */
public class JSONRPCException extends Exception {
    private Response.Error error;

    public JSONRPCException(Response.Error error) {
        super(String.format("code:%d %s", error.getCode(), error.getMessage()));

        this.error = error;
    }

    public Response.Error getError() {
        return error;
    }
}
