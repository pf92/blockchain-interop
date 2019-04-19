package com.ieee19.bc.interop.pf.core.model;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class represents a basic block of a blockchain.
 */
public class Block {

    private String hash;
    private String previousBlockHash;
    private long height;
    private ZonedDateTime timestamp;
    private int numberOfTransactions;
    private String minerAddress;
    private double difficulty;
    private List<Block> uncleBlocks = new ArrayList<>();

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreviousBlockHash() {
        return previousBlockHash;
    }

    public void setPreviousBlockHash(String previousBlockHash) {
        this.previousBlockHash = previousBlockHash;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getNumberOfTransactions() {
        return numberOfTransactions;
    }

    public void setNumberOfTransactions(int numberOfTransactions) {
        this.numberOfTransactions = numberOfTransactions;
    }

    public String getMinerAddress() {
        return minerAddress;
    }

    public void setMinerAddress(String minerAddress) {
        this.minerAddress = minerAddress;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
    }

    public List<Block> getUncleBlocks() {
        return uncleBlocks;
    }

    public void setUncleBlocks(List<Block> uncleBlocks) {
        this.uncleBlocks = uncleBlocks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Block)) return false;
        Block block = (Block) o;
        return Objects.equals(hash, block.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public String toString() {
        return "Block{" +
                "hash='" + hash + '\'' +
                ", previousBlockHash='" + previousBlockHash + '\'' +
                ", height=" + height +
                ", timestamp=" + timestamp +
                ", numberOfTransactions=" + numberOfTransactions +
                ", minerAddress='" + minerAddress + '\'' +
                ", difficulty=" + difficulty +
                ", uncleBlocks=" + uncleBlocks.size() +
                '}';
    }

}
