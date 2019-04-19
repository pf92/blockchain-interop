package com.ieee19.bc.interop.pf.proxy.bitcoin;

import com.ieee19.bc.interop.pf.proxy.bitcoin.exception.BitcoinException;
import com.ieee19.bc.interop.pf.proxy.currency.CryptocurrencyPriceService;
import com.ieee19.bc.interop.pf.proxy.currency.Currency;
import com.ieee19.bc.interop.pf.proxy.currency.CurrencyServiceException;
import com.ieee19.bc.interop.pf.proxy.currency.interfaces.ICryptocurrencyPriceService;
import io.reactivex.observers.TestObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BitcoinMetricCollectorTest {

    private BitcoinService bitcoinServiceMock;
    private ICryptocurrencyPriceService priceServiceMock;
    private BitcoinMetricCollector metricCollector;

    @BeforeEach
    public void setUp() {
        this.bitcoinServiceMock = mock(BitcoinService.class);
        this.priceServiceMock = mock(CryptocurrencyPriceService.class);
        this.metricCollector = new BitcoinMetricCollector(bitcoinServiceMock, priceServiceMock, Currency.US_DOLLAR, 5);
    }

    @Test
    public void testFailure_shouldThrowException() throws BitcoinException, CurrencyServiceException {
        when(bitcoinServiceMock.getCurrentBlockHeight())
                .thenReturn(5000L);
        when(bitcoinServiceMock.getBlockByBlockNumber(anyLong()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        when(priceServiceMock.getPrice(Currency.BITCOIN, Currency.US_DOLLAR))
                .thenReturn(BigDecimal.ONE);
        TestObserver<Double> testObserver = new TestObserver<>();

        metricCollector
                .getAvgBlockTimeObservable()
                .blockingSubscribe(testObserver);
        testObserver
                .assertError(HttpClientErrorException.class);
    }

}
