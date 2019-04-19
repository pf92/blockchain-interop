package com.ieee19.bc.interop.pf.core.exception;

/**
 * This exceptions indicates an error during a data fetch operation.
 */
public class DataReadingFailedException extends Exception {

    public DataReadingFailedException(String message) {
        super(message);
    }

    public DataReadingFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataReadingFailedException(Throwable cause) {
        super(cause);
    }

    public DataReadingFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
