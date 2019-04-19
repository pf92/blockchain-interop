package com.ieee19.bc.interop.pf.proxy.ethereum.dto.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonRpcTransaction {

    @JsonProperty("from")
    private String fromAddress;
    @JsonProperty("hash")
    private String hash;
    @JsonProperty("to")
    private String toAddress;
    @JsonProperty("input")
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
        if (!(o instanceof JsonRpcTransaction)) return false;
        JsonRpcTransaction that = (JsonRpcTransaction) o;
        return Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public String toString() {
        return "JsonRpcTransaction{" +
                "fromAddress='" + fromAddress + '\'' +
                ", hash='" + hash + '\'' +
                ", toAddress='" + toAddress + '\'' +
                ", data='" + data + '\'' +
                '}';
    }

}
