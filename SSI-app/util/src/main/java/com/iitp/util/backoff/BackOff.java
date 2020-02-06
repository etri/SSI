package com.iitp.util.backoff;

/**
 * Back-off
 */
public interface BackOff {
    public BackOffExecution start();
}
