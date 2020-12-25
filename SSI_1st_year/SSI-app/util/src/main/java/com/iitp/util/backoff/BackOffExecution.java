package com.iitp.util.backoff;

/**
 * back-off execute interface
 */
public interface BackOffExecution {
    long STOP = -1;

    public long nextBackOff();
}
