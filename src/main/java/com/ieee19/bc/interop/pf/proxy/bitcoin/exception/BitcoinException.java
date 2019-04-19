package com.ieee19.bc.interop.pf.proxy.bitcoin.exception;

/**
 * Indicates errors that have been occured during the interaction with the Bitcoin network.
 */
public class BitcoinException extends Exception {

    public BitcoinException(String message) {
        super(message);
    }

    public BitcoinException(String message, Throwable cause) {
        super(message, cause);
    }

    public BitcoinException(Throwable cause) {
        super(cause);
    }

    public BitcoinException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
