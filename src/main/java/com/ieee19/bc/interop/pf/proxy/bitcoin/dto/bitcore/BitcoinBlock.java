package com.ieee19.bc.interop.pf.proxy.bitcoin.dto.bitcore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BitcoinBlock {

    private String hash;

    @JsonProperty("height")
    private long blockNumber;

    @JsonProperty("tx")
    private List<String> transactionHashes = new ArrayList<>();

    @JsonProperty("time")
    private long timeInSeconds;   // unix epoch time, block creation time

    @JsonProperty("previousblockhash")
    private String previousBlockHash;

    private double difficulty;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public List<String> getTransactionHashes() {
        return transactionHashes;
    }

    public void setTransactionHashes(List<String> transactionHashes) {
        this.transactionHashes = transactionHashes;
    }

    public long getTimeInSeconds() {
        return timeInSeconds;
    }

    public void setTimeInSeconds(long timeInSeconds) {
        this.timeInSeconds = timeInSeconds;
    }

    public String getPreviousBlockHash() {
        return previousBlockHash;
    }

    public void setPreviousBlockHash(String previousBlockHash) {
        this.previousBlockHash = previousBlockHash;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BitcoinBlock)) return false;
        BitcoinBlock that = (BitcoinBlock) o;
        return Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public String toString() {
        return "BitcoinBlock{" +
                "hash='" + hash + '\'' +
                ", blockNumber=" + blockNumber +
                ", transactionHashes=" + transactionHashes.size() +
                ", timeInSeconds=" + timeInSeconds +
                ", previousBlockHash='" + previousBlockHash + '\'' +
                ", difficulty=" + difficulty +
                '}';
    }

}
