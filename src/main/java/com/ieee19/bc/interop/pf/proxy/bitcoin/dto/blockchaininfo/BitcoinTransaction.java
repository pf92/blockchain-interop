package com.ieee19.bc.interop.pf.proxy.bitcoin.dto.blockchaininfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BitcoinTransaction {

    @JsonProperty("out")
    private List<BitcoinTransactionOutput> transactionOutputs;

    public List<BitcoinTransactionOutput> getTransactionOutputs() {
        return transactionOutputs;
    }

    public void setTransactionOutputs(List<BitcoinTransactionOutput> transactionOutputs) {
        this.transactionOutputs = transactionOutputs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BitcoinTransaction)) return false;
        BitcoinTransaction that = (BitcoinTransaction) o;
        return Objects.equals(transactionOutputs, that.transactionOutputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionOutputs);
    }

    @Override
    public String toString() {
        return "BitcoinTransaction{" +
                "transactionOutputs=" + transactionOutputs +
                '}';
    }

}
