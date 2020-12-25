package com.iitp.iconloop.iitp.securities.exception;

public class IconServiceException extends Exception {

    public IconServiceException(String message, Throwable parent) {
        super(message, parent);
    }

    public IconServiceException(String message) {
        super(message);
    }

    public IconServiceException(Throwable t) {
        super(t);

    }
}