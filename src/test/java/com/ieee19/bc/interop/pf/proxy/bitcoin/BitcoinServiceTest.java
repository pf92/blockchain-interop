package com.ieee19.bc.interop.pf.proxy.bitcoin;

import com.ieee19.bc.interop.pf.core.model.Block;
import com.ieee19.bc.interop.pf.proxy.bitcoin.dto.bitcore.*;
import com.ieee19.bc.interop.pf.proxy.bitcoin.dto.blockcypher.FeePerKbInfo;
import com.ieee19.bc.interop.pf.proxy.bitcoin.exception.BitcoinException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.List;

import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BitcoinServiceTest {

    private static final String NODE_URL = "http://test.test:1234";
    private static final String BLOCKCYPHER_BASE_URL = "https://api.blockcypher.com/v1/btc/main/";
    private static final String PRIVATE_KEY = "cNrm92vMehg1zAYy95nrbi5yjkUAMsZzFomyV2m5Ew3FZ2ej7SBo";
    private static final long EXPECTED_BLOCK_HEIGHT = 123456;
    private static final String EXPECTED_BLOCK_HASH = "0000000000000000002589c00f688860f59eb806452c66c4a13887c22abf8f3c";
    private static final String EXPECTED_TX_HASH = "92744aca720821cc6b8a8af6da9ebb7da8e9464bd9f094f9f99ef64f326e2cc5";
    private static final String EXPECTED_ADDRESS = "mzfXmtEUXF95Yp9NAytnZBMbPpVeoHgeFL";

    private BitcoinService bitcoinService;
    private RestTemplate restTemplateMock;

    @BeforeEach
    public void setUp() {
        bitcoinService = new BitcoinService(NODE_URL, EXPECTED_ADDRESS, PRIVATE_KEY);
        restTemplateMock = mock(RestTemplate.class);
        bitcoinService.setRestTemplate(restTemplateMock);
    }

    @Test
    public void testGetCurrentBlockHeight_shouldReturnCorrectNumber() throws BitcoinException {
        BitcoinInfo bitcoinInfo = new BitcoinInfo();
        bitcoinInfo.setCurrentBlockHeight(EXPECTED_BLOCK_HEIGHT);

        when(restTemplateMock.getForObject(NODE_URL + "status?q=getInfo", BitcoinInfo.class))
                .thenReturn(bitcoinInfo);

        long currentBlockHeight = bitcoinService.getCurrentBlockHeight();

        verify(restTemplateMock).getForObject(anyString(), any());
        assertEquals(EXPECTED_BLOCK_HEIGHT, currentBlockHeight, "Block height must be " + EXPECTED_BLOCK_HEIGHT);
    }

    @Test
    public void testGetCurrentBlockHeight_shouldThrowException() throws BitcoinException {
        RestTemplate restTemplate = mock(RestTemplate.class);
        bitcoinService.setRestTemplate(restTemplate);

        when(restTemplate.getForObject(NODE_URL + "status?q=getInfo", BitcoinInfo.class))
                .thenThrow(HttpServerErrorException.class);

        assertThrows(BitcoinException.class, () -> bitcoinService.getCurrentBlockHeight());
        verify(restTemplate).getForObject(anyString(), any());
    }

    @Test
    public void testGetBlockByBlockNumber_shouldReturnCorrectBlock() throws BitcoinException {
        String response = "{ \"blockHash\": \"" + EXPECTED_BLOCK_HASH + "\" }";
        when(restTemplateMock.getForObject(NODE_URL + "block-index/" + EXPECTED_BLOCK_HEIGHT, String.class))
                .thenReturn(response);

        BitcoinBlock expectedBitcoinBlock = new BitcoinBlock();
        expectedBitcoinBlock.setHash(EXPECTED_BLOCK_HASH);
        expectedBitcoinBlock.getTransactionHashes().add(EXPECTED_TX_HASH);
        when(restTemplateMock.getForObject(NODE_URL + "block/" + EXPECTED_BLOCK_HASH, BitcoinBlock.class))
                .thenReturn(expectedBitcoinBlock);

        BitcoinScriptPubKey scriptPubKey = new BitcoinScriptPubKey();
        scriptPubKey.getAddresses().add(EXPECTED_ADDRESS);
        BitcoinTransactionOutput txOutput = new BitcoinTransactionOutput();
        txOutput.setScriptPubKey(scriptPubKey);
        BitcoinTransaction bitcoinTransaction = new BitcoinTransaction();
        bitcoinTransaction.setBlockHash(EXPECTED_BLOCK_HASH);
        bitcoinTransaction.setHash(EXPECTED_TX_HASH);
        bitcoinTransaction.setCoinBase(true);
        bitcoinTransaction.getTransactionOutputs().add(txOutput);
        when(restTemplateMock.getForObject(NODE_URL + "tx/" + EXPECTED_TX_HASH, BitcoinTransaction.class))
                .thenReturn(bitcoinTransaction);

        Block actualBlock = bitcoinService.getBlockByBlockNumber(EXPECTED_BLOCK_HEIGHT);

        verify(restTemplateMock, times(3)).getForObject(anyString(), any());
        assertEquals(EXPECTED_BLOCK_HASH, actualBlock.getHash(), "Block hashes differ!");
        assertEquals(EXPECTED_ADDRESS, actualBlock.getMinerAddress(), "Wrong miner address!");
        assertEquals(1, actualBlock.getNumberOfTransactions(), "Wrong tx count!");
    }

    @Test
    public void testGetFeePerKbInfo_shouldReturnCorrectResult() throws BitcoinException {
        FeePerKbInfo expectedFeePerKbInfo = new FeePerKbInfo();
        expectedFeePerKbInfo.setHighFeePerKb(10);
        expectedFeePerKbInfo.setMediumFeePerKb(5);
        expectedFeePerKbInfo.setLowFeePerKb(1);

        when(restTemplateMock.getForObject(BLOCKCYPHER_BASE_URL, FeePerKbInfo.class))
                .thenReturn(expectedFeePerKbInfo);

        FeePerKbInfo actualFeePerKbInfo = bitcoinService.getFeePerKbInfo();

        verify(restTemplateMock).getForObject(anyString(), any());
        assertEquals(expectedFeePerKbInfo, actualFeePerKbInfo, "Wrong fee info object returned!");
    }

    @Test
    public void testGetData_shouldReturnCorrectResult() throws BitcoinException {
        BitcoinAddressTransactions addrTx = new BitcoinAddressTransactions();
        BitcoinTransaction bitcoinTransaction = new BitcoinTransaction();
        BitcoinScriptPubKey scriptPubKey = new BitcoinScriptPubKey();
        scriptPubKey.setHex("6a03313131");  // OP_RETURN wit data "111"
        BitcoinTransactionOutput txOutput = new BitcoinTransactionOutput();
        txOutput.setScriptPubKey(scriptPubKey);

        bitcoinTransaction.getTransactionOutputs().add(txOutput);
        txOutput = new BitcoinTransactionOutput();
        scriptPubKey = new BitcoinScriptPubKey();
        scriptPubKey.setHex("6a0461736466");  // OP_RETURN wit data "asdf"
        txOutput.setScriptPubKey(scriptPubKey);
        bitcoinTransaction.getTransactionOutputs().add(txOutput);
        bitcoinTransaction.setBlockTime(System.currentTimeMillis() / 1000L);  // now
        addrTx.getTransactions().add(bitcoinTransaction);

        bitcoinTransaction = new BitcoinTransaction();
        txOutput = new BitcoinTransactionOutput();
        scriptPubKey = new BitcoinScriptPubKey();
        scriptPubKey.setHex("6a046b6a6c68");  // OP_RETURN wit data "kjlh"
        txOutput.setScriptPubKey(scriptPubKey);
        bitcoinTransaction.getTransactionOutputs().add(txOutput);
        bitcoinTransaction.setBlockTime(System.currentTimeMillis() / 1000L);  // now
        addrTx.getTransactions().add(bitcoinTransaction);

        when(restTemplateMock.getForObject(NODE_URL + "txs?address=" + EXPECTED_ADDRESS, BitcoinAddressTransactions.class))
                .thenReturn(addrTx);

        List<String> actualData = bitcoinService.getData(
                ZonedDateTime.now().minus(1, HOURS),
                ZonedDateTime.now().plus(1, HOURS)
        );

        verify(restTemplateMock).getForObject(anyString(), any());
        assertTrue(actualData.size() == 3, "Data list must contain exactly 3 elements!");
        assertTrue(actualData.contains("111"), "Data list must contain 111");
        assertTrue(actualData.contains("asdf"), "Data list must contain asdf");
        assertTrue(actualData.contains("kjlh"), "Data list must contain kjlh");
    }

    @Test
    public void testStoreDataWithNoUtxo_shouldShouldThrowException() throws BitcoinException {
        when(restTemplateMock.getForObject(NODE_URL + "addr/" + EXPECTED_ADDRESS + "/utxo",
                BitcoinUnspentTransactionOutput[].class))
                .thenReturn(new BitcoinUnspentTransactionOutput[0]);

        assertThrows(BitcoinException.class, () -> bitcoinService.storeData("", 1));
    }

}
