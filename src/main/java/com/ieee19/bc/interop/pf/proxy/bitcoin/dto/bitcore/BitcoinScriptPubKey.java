package com.ieee19.bc.interop.pf.proxy.bitcoin.dto.bitcore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BitcoinScriptPubKey {

    private String hex;

    private String asm;

    List<String> addresses = new ArrayList<>();

    public String getHex() {
        return hex;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    public String getAsm() {
        return asm;
    }

    public void setAsm(String asm) {
        this.asm = asm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BitcoinScriptPubKey)) return false;
        BitcoinScriptPubKey that = (BitcoinScriptPubKey) o;
        return Objects.equals(hex, that.hex) &&
                Objects.equals(asm, that.asm) &&
                Objects.equals(addresses, that.addresses);
    }

    @Override
    public int hashCode() {

        return Objects.hash(hex, asm, addresses);
    }

    @Override
    public String toString() {
        return "BitcoinScriptPubKey{" +
                "hex='" + hex + '\'' +
                ", asm='" + asm + '\'' +
                ", addresses=" + addresses +
                '}';
    }

}
