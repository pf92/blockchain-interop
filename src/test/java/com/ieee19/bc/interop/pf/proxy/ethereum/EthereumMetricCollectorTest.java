package com.ieee19.bc.interop.pf.proxy.ethereum;

import com.ieee19.bc.interop.pf.proxy.currency.CryptocurrencyPriceService;
import com.ieee19.bc.interop.pf.proxy.currency.Currency;
import com.ieee19.bc.interop.pf.proxy.currency.CurrencyServiceException;
import com.ieee19.bc.interop.pf.proxy.currency.interfaces.ICryptocurrencyPriceService;
import com.ieee19.bc.interop.pf.proxy.ethereum.exception.EthereumException;
import io.reactivex.observers.TestObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EthereumMetricCollectorTest {

    private EthereumService ethereumService;
    private ICryptocurrencyPriceService priceServiceMock;
    private EthereumMetricCollector metricCollector;

    @BeforeEach
    public void setUp() {
        this.ethereumService = mock(EthereumService.class);
        this.priceServiceMock = mock(CryptocurrencyPriceService.class);
        this.metricCollector = new EthereumMetricCollector(ethereumService, priceServiceMock, Currency.US_DOLLAR, 5);
    }

    @Test
    public void testFailure_shouldThrowException() throws CurrencyServiceException, EthereumException {
        when(ethereumService.getCurrentBlockNumber())
                .thenReturn(5000L);
        when(ethereumService.getBlockByNumberWithUncles(anyLong()))
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
