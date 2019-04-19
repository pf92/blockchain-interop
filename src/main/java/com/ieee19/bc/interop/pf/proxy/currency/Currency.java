package com.ieee19.bc.interop.pf.proxy.currency;

public enum Currency {
    EURO("EUR"),
    US_DOLLAR("USD"),
    BITCOIN("BTC"),
    ETHER("ETH"),
    EXPANSE("EXP"),
    ETHEREUM_CLASSIC("ETC");

    private String label;

    Currency(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
