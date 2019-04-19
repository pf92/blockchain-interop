package com.ieee19.bc.interop.pf.proxy.bitcoin;

import com.ieee19.bc.interop.pf.core.exception.DataReadingFailedException;
import com.ieee19.bc.interop.pf.core.exception.DataWritingFailedException;
import com.ieee19.bc.interop.pf.proxy.bitcoin.dto.blockcypher.FeePerKbInfo;
import com.ieee19.bc.interop.pf.proxy.bitcoin.exception.BitcoinException;
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

public class BitcoinDataAccessServiceTest {

    private static final int EXPECTED_FEES = 500;
    private static final int MAX_TX_DATA_LENGTH = 40;  // in bytes
    private static final String EXPECTED_DATA_STR1 = "test 1";
    private static final String EXPECTED_DATA_STR2 = "test 2";
    private static final String EXPECTED_DATA_STR3 = "test 3";

    private BitcoinDataAccessService dataAccessService;
    private BitcoinService bitcoinServiceMock;
    private BiFunction<String, Integer, List<String>> singleDataStringFormatterMock;
    private BiFunction<List<String>, Integer, List<String>> dataStringListFormatterMock;

    @BeforeEach
    public void setUp() {
        this.bitcoinServiceMock = mock(BitcoinService.class);
        this.singleDataStringFormatterMock = mock(BiFunction.class);
        this.dataStringListFormatterMock = mock(BiFunction.class);
        this.dataAccessService = new BitcoinDataAccessService(singleDataStringFormatterMock,
                dataStringListFormatterMock, bitcoinServiceMock);
    }

    @Test
    public void testGetFeesInSatoshis_shouldCallBitcoinService() throws BitcoinException {
        FeePerKbInfo feePerKbInfo = new FeePerKbInfo();
        feePerKbInfo.setMediumFeePerKb(EXPECTED_FEES);
        Mockito.when(bitcoinServiceMock.getFeePerKbInfo())
                .thenReturn(feePerKbInfo);

        long actualFees = dataAccessService.getFeesInSatoshis();
        assertEquals(EXPECTED_FEES, actualFees, "Actual fees not matching expected fees!");
        verify(bitcoinServiceMock).getFeePerKbInfo();
    }

    @Test
    public void testGetFeesInSatoshis_shouldReturnUserSetValue() throws BitcoinException {
        dataAccessService.setFeesInSatoshis(EXPECTED_FEES);

        long actualFees = dataAccessService.getFeesInSatoshis();
        assertEquals(EXPECTED_FEES, actualFees, "Actual fees not matching expected fees!");
        verify(bitcoinServiceMock, times(0)).getFeePerKbInfo();
    }

    @Test
    public void testGetData_shouldReturnCorrectResult() throws DataReadingFailedException, BitcoinException {
        List<String> expectedDataStrings = Arrays.asList(EXPECTED_DATA_STR1, EXPECTED_DATA_STR2, EXPECTED_DATA_STR3);

        when(bitcoinServiceMock.getData(any(ZonedDateTime.class), any(ZonedDateTime.class)))
                .thenReturn(expectedDataStrings);

        List<String> actualDataStrings = dataAccessService.getData(ZonedDateTime.now(), ZonedDateTime.now());
        assertEquals(expectedDataStrings, actualDataStrings, "Data strings must match!");
        verify(bitcoinServiceMock, times(1)).getData(any(ZonedDateTime.class), any(ZonedDateTime.class));
    }

    @Test
    public void testWriteData_shouldCallCorrectMethods() throws DataWritingFailedException, BitcoinException {
        List<String> expectedData = Arrays.asList(EXPECTED_DATA_STR1, EXPECTED_DATA_STR2);
        dataAccessService.setFeesInSatoshis(EXPECTED_FEES);
        doNothing().when(bitcoinServiceMock).storeData(anyString(), anyLong());
        when(singleDataStringFormatterMock.apply(EXPECTED_DATA_STR1, MAX_TX_DATA_LENGTH))
                .thenReturn(expectedData);

        dataAccessService.writeData(EXPECTED_DATA_STR1);
        verify(singleDataStringFormatterMock, times(1)).apply(EXPECTED_DATA_STR1, MAX_TX_DATA_LENGTH);
        verify(bitcoinServiceMock, times(1)).storeData(EXPECTED_DATA_STR1, EXPECTED_FEES);
        verify(bitcoinServiceMock, times(1)).storeData(EXPECTED_DATA_STR2, EXPECTED_FEES);
    }

    @Test
    public void testWriteData_shouldCallNoMethods() throws DataWritingFailedException, BitcoinException {
        List<String> expectedData = Collections.emptyList();
        dataAccessService.setFeesInSatoshis(EXPECTED_FEES);
        when(singleDataStringFormatterMock.apply(EXPECTED_DATA_STR1, MAX_TX_DATA_LENGTH))
                .thenReturn(expectedData);

        dataAccessService.writeData(EXPECTED_DATA_STR1);
        verify(singleDataStringFormatterMock, times(1)).apply(EXPECTED_DATA_STR1, MAX_TX_DATA_LENGTH);
        verify(bitcoinServiceMock, times(0)).storeData(anyString(), anyLong());
    }

    @Test
    public void testWriteDataList_shouldCallCorrectMethods() throws DataWritingFailedException, BitcoinException {
        List<String> expectedData = Arrays.asList(EXPECTED_DATA_STR1, EXPECTED_DATA_STR2);
        dataAccessService.setFeesInSatoshis(EXPECTED_FEES);
        doNothing().when(bitcoinServiceMock).storeData(anyString(), anyLong());
        when(dataStringListFormatterMock.apply(expectedData, MAX_TX_DATA_LENGTH))
                .thenReturn(expectedData);

        dataAccessService.writeData(expectedData);
        verify(dataStringListFormatterMock, times(1)).apply(expectedData, MAX_TX_DATA_LENGTH);
        verify(bitcoinServiceMock, times(1)).storeData(EXPECTED_DATA_STR1, EXPECTED_FEES);
        verify(bitcoinServiceMock, times(1)).storeData(EXPECTED_DATA_STR2, EXPECTED_FEES);
    }

}
