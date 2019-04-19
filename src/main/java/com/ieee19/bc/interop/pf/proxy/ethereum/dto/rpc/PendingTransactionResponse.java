package com.ieee19.bc.interop.pf.proxy.ethereum.dto.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PendingTransactionResponse {

    @JsonProperty("result")
    private List<JsonRpcTransaction> pendingTransactions = new ArrayList<>();

    public List<JsonRpcTransaction> getPendingTransactions() {
        return pendingTransactions;
    }

    public void setPendingTransactions(List<JsonRpcTransaction> pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }

    @Override
    public String toString() {
        return "PendingTransactionResponse{" +
                "pendingTransactions=" + pendingTransactions.size() +
                '}';
    }

}
