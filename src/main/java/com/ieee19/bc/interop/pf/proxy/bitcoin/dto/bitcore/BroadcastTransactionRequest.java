package com.ieee19.bc.interop.pf.proxy.bitcoin.dto.bitcore;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BroadcastTransactionRequest {

    @JsonProperty("rawtx")
    private String rawTransaction;

    public String getRawTransaction() {
        return rawTransaction;
    }

    public void setRawTransaction(String rawTransaction) {
        this.rawTransaction = rawTransaction;
    }

    @Override
    public String toString() {
        return "BroadcastTransactionRequest{" +
                "rawTransaction='" + rawTransaction + '\'' +
                '}';
    }

}
