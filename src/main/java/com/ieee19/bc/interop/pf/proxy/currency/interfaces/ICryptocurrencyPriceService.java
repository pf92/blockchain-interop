package com.ieee19.bc.interop.pf.proxy.currency.interfaces;

import com.ieee19.bc.interop.pf.proxy.currency.Currency;
import com.ieee19.bc.interop.pf.proxy.currency.CurrencyServiceException;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;

/**
 * This interface represents a service for retrieving exchange rates.
 */
public interface ICryptocurrencyPriceService {

    /**
     * @param from the currency you have
     * @param to the currency you want
     * @return returns the exchange rate
     * @throws IOException
     * @throws URISyntaxException
     */
    BigDecimal getPrice(Currency from, Currency to) throws CurrencyServiceException;

}
