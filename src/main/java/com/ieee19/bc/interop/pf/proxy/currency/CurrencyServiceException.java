package com.ieee19.bc.interop.pf.proxy.currency;

public class CurrencyServiceException extends Exception {

    public CurrencyServiceException(String message) {
        super(message);
    }

    public CurrencyServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CurrencyServiceException(Throwable cause) {
        super(cause);
    }

    public CurrencyServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
