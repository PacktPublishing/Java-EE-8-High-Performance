package com.github.rmannibucau.failover.cdi.internal;

public class FailoverException extends RuntimeException {
    FailoverException(final String message) {
        super(message);
    }
}
