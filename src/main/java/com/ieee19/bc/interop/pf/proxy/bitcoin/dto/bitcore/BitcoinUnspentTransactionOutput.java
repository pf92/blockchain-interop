package com.ieee19.bc.interop.pf.proxy.bitcoin.dto.bitcore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BitcoinUnspentTransactionOutput {

    @JsonProperty("txid")
    private String transactionId;

    @JsonProperty("vout")
    private int txOutputNumber;

    @JsonProperty("scriptPubKey")
    private String scriptPubKey;

    @JsonProperty("satoshis")
    private long satoshis;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getTxOutputNumber() {
        return txOutputNumber;
    }

    public void setTxOutputNumber(int txOutputNumber) {
        this.txOutputNumber = txOutputNumber;
    }

    public String getScriptPubKey() {
        return scriptPubKey;
    }

    public void setScriptPubKey(String scriptPubKey) {
        this.scriptPubKey = scriptPubKey;
    }

    public long getSatoshis() {
        return satoshis;
    }

    public void setSatoshis(long satoshis) {
        this.satoshis = satoshis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BitcoinUnspentTransactionOutput)) return false;
        BitcoinUnspentTransactionOutput that = (BitcoinUnspentTransactionOutput) o;
        return Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        return "BitcoinUnspentTransactionOutput{" +
                "transactionId='" + transactionId + '\'' +
                ", txOutputNumber=" + txOutputNumber +
                ", scriptPubKey='" + scriptPubKey + '\'' +
                ", satoshis=" + satoshis +
                '}';
    }

}
