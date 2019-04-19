package com.ieee19.bc.interop.pf.proxy.bitcoin.dto.blockchaininfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BitcoinTransactionOutput {

    @JsonProperty("addr")
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BitcoinTransactionOutput)) return false;
        BitcoinTransactionOutput that = (BitcoinTransactionOutput) o;
        return Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }

    @Override
    public String toString() {
        return "BitcoinTransactionOutput{" +
                "address='" + address + '\'' +
                '}';
    }

}
