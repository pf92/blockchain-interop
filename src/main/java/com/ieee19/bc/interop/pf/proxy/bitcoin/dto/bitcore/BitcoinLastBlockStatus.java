package com.ieee19.bc.interop.pf.proxy.bitcoin.dto.bitcore;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class BitcoinLastBlockStatus {

    @JsonProperty("lastblockhash")
    private String lastBlockHash;

    public String getLastBlockHash() {
        return lastBlockHash;
    }

    public void setLastBlockHash(String lastBlockHash) {
        this.lastBlockHash = lastBlockHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BitcoinLastBlockStatus)) return false;
        BitcoinLastBlockStatus that = (BitcoinLastBlockStatus) o;
        return Objects.equals(lastBlockHash, that.lastBlockHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastBlockHash);
    }

    @Override
    public String toString() {
        return "BitcoinLastBlockStatus{" +
                "lastBlockHash='" + lastBlockHash + '\'' +
                '}';
    }

}
