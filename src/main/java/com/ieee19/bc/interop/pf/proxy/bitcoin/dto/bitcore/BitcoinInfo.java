package com.ieee19.bc.interop.pf.proxy.bitcoin.dto.bitcore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * This class represents a simplified response of a api call to insight-api/status?q=getInfo.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BitcoinInfo {

    private long currentBlockHeight;

    @JsonProperty("info")
    private void unpackNestedInfo(Map<String, Object> info) {
        currentBlockHeight = (int) info.get("blocks");
    }

    public long getCurrentBlockHeight() {
        return currentBlockHeight;
    }

    public void setCurrentBlockHeight(long currentBlockHeight) {
        this.currentBlockHeight = currentBlockHeight;
    }

    @Override
    public String toString() {
        return "BitcoinInfo{" +
                "currentBlockHeight=" + currentBlockHeight +
                '}';
    }

}
