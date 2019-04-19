package com.ieee19.bc.interop.pf.proxy.bitcoin.dto.bitcore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BitcoinTransactionOutput {

    @JsonProperty("value")
    private double valueInBTC;

    private BitcoinScriptPubKey scriptPubKey;

    public double getValueInBTC() {
        return valueInBTC;
    }

    public BitcoinScriptPubKey getScriptPubKey() {
        return scriptPubKey;
    }

    public void setScriptPubKey(BitcoinScriptPubKey scriptPubKey) {
        this.scriptPubKey = scriptPubKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BitcoinTransactionOutput)) return false;
        BitcoinTransactionOutput that = (BitcoinTransactionOutput) o;
        return Double.compare(that.valueInBTC, valueInBTC) == 0 &&
                Objects.equals(scriptPubKey, that.scriptPubKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueInBTC, scriptPubKey);
    }

    @Override
    public String toString() {
        return "BitcoinTransactionOutput{" +
                "valueInBTC=" + valueInBTC +
                ", scriptPubKey=" + scriptPubKey +
                '}';
    }

}
