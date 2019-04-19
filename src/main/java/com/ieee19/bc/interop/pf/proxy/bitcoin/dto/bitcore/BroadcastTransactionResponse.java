package com.ieee19.bc.interop.pf.proxy.bitcoin.dto.bitcore;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BroadcastTransactionResponse {

    @JsonProperty("txid")
    private String tansactionId;

    public String getTansactionId() {
        return tansactionId;
    }

    public void setTansactionId(String tansactionId) {
        this.tansactionId = tansactionId;
    }

    @Override
    public String toString() {
        return "BroadcastTransactionResponse{" +
                "tansactionId='" + tansactionId + '\'' +
                '}';
    }

}
