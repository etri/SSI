package com.iitp.core.crypto;

/**
 * Exception is duplicate key in {@link KeyManager}<br>
 */
public class DuplicatedKeyException extends Exception {
    DuplicatedKeyException(String message) {
        super(message);
    }
}
