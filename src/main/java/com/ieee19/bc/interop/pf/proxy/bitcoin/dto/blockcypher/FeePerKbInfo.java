package com.ieee19.bc.interop.pf.proxy.bitcoin.dto.blockcypher;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeePerKbInfo {

    @JsonProperty("high_fee_per_kb")
    private int highFeePerKb;  // A rolling average of the fee (in satoshis) paid per kilobyte for transactions to be confirmed within 1 to 2 blocks.

    @JsonProperty("medium_fee_per_kb")
    private int mediumFeePerKb;  // A rolling average of the fee (in satoshis) paid per kilobyte for transactions to be confirmed within 3 to 6 blocks.

    @JsonProperty("low_fee_per_kb")
    private int lowFeePerKb;  // A rolling average of the fee (in satoshis) paid per kilobyte for transactions to be confirmed in 7 or more blocks.

    public int getHighFeePerKb() {
        return highFeePerKb;
    }

    public void setHighFeePerKb(int highFeePerKb) {
        this.highFeePerKb = highFeePerKb;
    }

    public int getMediumFeePerKb() {
        return mediumFeePerKb;
    }

    public void setMediumFeePerKb(int mediumFeePerKb) {
        this.mediumFeePerKb = mediumFeePerKb;
    }

    public int getLowFeePerKb() {
        return lowFeePerKb;
    }

    public void setLowFeePerKb(int lowFeePerKb) {
        this.lowFeePerKb = lowFeePerKb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FeePerKbInfo)) return false;
        FeePerKbInfo that = (FeePerKbInfo) o;
        return highFeePerKb == that.highFeePerKb &&
                mediumFeePerKb == that.mediumFeePerKb &&
                lowFeePerKb == that.lowFeePerKb;
    }

    @Override
    public int hashCode() {
        return Objects.hash(highFeePerKb, mediumFeePerKb, lowFeePerKb);
    }

    @Override
    public String toString() {
        return "FeePerKbInfo{" +
                "highFeePerKb=" + highFeePerKb +
                ", mediumFeePerKb=" + mediumFeePerKb +
                ", lowFeePerKb=" + lowFeePerKb +
                '}';
    }

}
