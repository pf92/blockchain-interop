package com.ieee19.bc.interop.pf.proxy.ethereum.model;

import java.util.Objects;

public class Transaction {

    private String fromAddress;
    private String hash;
    private String toAddress;
    private String data;  // in hex representation

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "fromAddress='" + fromAddress + '\'' +
                ", hash='" + hash + '\'' +
                ", toAddress='" + toAddress + '\'' +
                ", data='" + data + '\'' +
                '}';
    }

}
