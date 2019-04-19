package com.ieee19.bc.interop.pf.proxy.ethereum.exception;

/**
 * Indicates errors that have been occured during the interaction with the Ethereum network.
 */
public class EthereumException extends Exception {

    public EthereumException(String message) {
        super(message);
    }

    public EthereumException(String message, Throwable cause) {
        super(message, cause);
    }

    public EthereumException(Throwable cause) {
        super(cause);
    }

    public EthereumException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
