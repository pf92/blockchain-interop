package com.ieee19.bc.interop.pf.proxy.bitcoin.dto.bitcore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BitcoinTransaction {

    @JsonProperty("txid")
    private String hash;

    @JsonProperty("blockhash")
    private String blockHash;

    @JsonProperty("isCoinBase")
    private boolean isCoinBase;

    @JsonProperty("vout")
    private List<BitcoinTransactionOutput> transactionOutputs = new ArrayList<>();

    @JsonProperty("blocktime")
    private long blockTime;  // unix epoch blockTime

    @JsonProperty("confirmations")
    private int confirmations;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public boolean isCoinBase() {
        return isCoinBase;
    }

    public void setCoinBase(boolean coinBase) {
        isCoinBase = coinBase;
    }

    public List<BitcoinTransactionOutput> getTransactionOutputs() {
        return transactionOutputs;
    }

    public void setTransactionOutputs(List<BitcoinTransactionOutput> transactionOutputs) {
        this.transactionOutputs = transactionOutputs;
    }

    public long getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(long blockTime) {
        this.blockTime = blockTime;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BitcoinTransaction)) return false;
        BitcoinTransaction that = (BitcoinTransaction) o;
        return Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public String toString() {
        return "BitcoinTransaction{" +
                "hash='" + hash + '\'' +
                ", blockHash='" + blockHash + '\'' +
                ", isCoinBase=" + isCoinBase +
                ", transactionOutputs=" + transactionOutputs +
                ", blockTime=" + blockTime +
                ", confirmations=" + confirmations +
                '}';
    }

}
