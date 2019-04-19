package com.ieee19.bc.interop.pf.core.exception;

/**
 * This exceptions indicates an error during a store operation.
 */
public class DataWritingFailedException extends Exception {

    public DataWritingFailedException(String message) {
        super(message);
    }

    public DataWritingFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataWritingFailedException(Throwable cause) {
        super(cause);
    }

    public DataWritingFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
