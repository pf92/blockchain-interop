package com.ieee19.bc.interop.pf.proxy.ethereum;

import com.ieee19.bc.interop.pf.core.exception.DataReadingFailedException;
import com.ieee19.bc.interop.pf.core.exception.DataWritingFailedException;
import com.ieee19.bc.interop.pf.proxy.ethereum.exception.EthereumException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EthereumDataAccessServiceTest {

    private static final long EXPECTED_GAS_PRICE = 5000000000L;
    private static final int MAX_TX_DATA_LENGTH = 40;  // in bytes
    private static final String EXPECTED_DATA_STR1 = "test 1";
    private static final String EXPECTED_DATA_STR2 = "test 2";
    private static final String EXPECTED_DATA_STR3 = "test 3";

    private EthereumDataAccessService dataAccessService;
    private EthereumService ethereumServiceMock;
    private BiFunction<String, Integer, List<String>> singleDataStringFormatterMock;
    private BiFunction<List<String>, Integer, List<String>> dataStringListFormatterMock;

    @BeforeEach
    public void setUp() {
        this.ethereumServiceMock = mock(EthereumService.class);
        this.singleDataStringFormatterMock = mock(BiFunction.class);
        this.dataStringListFormatterMock = mock(BiFunction.class);
        this.dataAccessService = new EthereumDataAccessService(singleDataStringFormatterMock,
                dataStringListFormatterMock, ethereumServiceMock);
    }

    @Test
    public void testGetGasPriceInWei_shouldCallEthereumService() throws EthereumException {
        Mockito.when(ethereumServiceMock.getGasPrice())
                .thenReturn(EXPECTED_GAS_PRICE);

        long actualGasPrice = dataAccessService.getGasPriceInWei();
        assertEquals(EXPECTED_GAS_PRICE, actualGasPrice, "Actual gas price not matching!");
        verify(ethereumServiceMock).getGasPrice();
    }

    @Test
    public void testGetGasPriceInWei_shouldReturnUserSetValue() throws EthereumException {
        dataAccessService.setGasPriceInWei(EXPECTED_GAS_PRICE);

        long actualGasPrice = dataAccessService.getGasPriceInWei();
        assertEquals(EXPECTED_GAS_PRICE, actualGasPrice, "Actual gas price not matching!");
        verify(ethereumServiceMock, times(0)).getGasPrice();
    }

    @Test
    public void testGetData_shouldReturnCorrectResult() throws DataReadingFailedException, EthereumException {
        List<String> expectedDataStrings = Arrays.asList(EXPECTED_DATA_STR1, EXPECTED_DATA_STR2, EXPECTED_DATA_STR3);

        when(ethereumServiceMock.getData(any(ZonedDateTime.class), any(ZonedDateTime.class)))
                .thenReturn(expectedDataStrings);

        List<String> actualDataStrings = dataAccessService.getData(ZonedDateTime.now(), ZonedDateTime.now());
        assertEquals(expectedDataStrings, actualDataStrings, "Data strings must match!");
        verify(ethereumServiceMock, times(1)).getData(any(ZonedDateTime.class), any(ZonedDateTime.class));
    }

    @Test
    public void testWriteData_shouldCallCorrectMethods() throws DataWritingFailedException, EthereumException {
        List<String> expectedData = Arrays.asList(EXPECTED_DATA_STR1, EXPECTED_DATA_STR2);
        dataAccessService.setGasPriceInWei(EXPECTED_GAS_PRICE);
        doNothing().when(ethereumServiceMock).storeData(anyString(), anyLong());
        when(ethereumServiceMock.getMaxTransactionDataSizeInBytes()).thenReturn(MAX_TX_DATA_LENGTH);
        when(singleDataStringFormatterMock.apply(EXPECTED_DATA_STR1, MAX_TX_DATA_LENGTH))
                .thenReturn(expectedData);

        dataAccessService.writeData(EXPECTED_DATA_STR1);
        verify(singleDataStringFormatterMock, times(1)).apply(EXPECTED_DATA_STR1, MAX_TX_DATA_LENGTH);
        verify(ethereumServiceMock, times(1)).storeData(EXPECTED_DATA_STR1, EXPECTED_GAS_PRICE);
        verify(ethereumServiceMock, times(1)).storeData(EXPECTED_DATA_STR2, EXPECTED_GAS_PRICE);
    }

    @Test
    public void testWriteData_shouldCallNoMethods() throws DataWritingFailedException, EthereumException {
        List<String> expectedData = Collections.emptyList();
        dataAccessService.setGasPriceInWei(EXPECTED_GAS_PRICE);
        when(ethereumServiceMock.getMaxTransactionDataSizeInBytes()).thenReturn(MAX_TX_DATA_LENGTH);
        when(singleDataStringFormatterMock.apply(EXPECTED_DATA_STR1, MAX_TX_DATA_LENGTH))
                .thenReturn(expectedData);

        dataAccessService.writeData(EXPECTED_DATA_STR1);
        verify(singleDataStringFormatterMock, times(1)).apply(EXPECTED_DATA_STR1, MAX_TX_DATA_LENGTH);
        verify(ethereumServiceMock, times(0)).storeData(anyString(), anyLong());
    }

    @Test
    public void testWriteDataList_shouldCallCorrectMethods() throws DataWritingFailedException, EthereumException {
        List<String> expectedData = Arrays.asList(EXPECTED_DATA_STR1, EXPECTED_DATA_STR2);
        dataAccessService.setGasPriceInWei(EXPECTED_GAS_PRICE);
        doNothing().when(ethereumServiceMock).storeData(anyString(), anyLong());
        when(ethereumServiceMock.getMaxTransactionDataSizeInBytes()).thenReturn(MAX_TX_DATA_LENGTH);
        when(dataStringListFormatterMock.apply(expectedData, MAX_TX_DATA_LENGTH))
                .thenReturn(expectedData);

        dataAccessService.writeData(expectedData);
        verify(dataStringListFormatterMock, times(1)).apply(expectedData, MAX_TX_DATA_LENGTH);
        verify(ethereumServiceMock, times(1)).storeData(EXPECTED_DATA_STR1, EXPECTED_GAS_PRICE);
        verify(ethereumServiceMock, times(1)).storeData(EXPECTED_DATA_STR2, EXPECTED_GAS_PRICE);
    }

}
