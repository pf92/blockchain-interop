package com.ieee19.bc.interop.pf.proxy.bitcoin.dto.bitcore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BitcoinAddressTransactions {

    @JsonProperty("txs")
    private List<BitcoinTransaction> transactions = new ArrayList<>();

    public List<BitcoinTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<BitcoinTransaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public String toString() {
        return "BitcoinAddressTransactions{" +
                "transactions=" + transactions.size() +
                '}';
    }

}
