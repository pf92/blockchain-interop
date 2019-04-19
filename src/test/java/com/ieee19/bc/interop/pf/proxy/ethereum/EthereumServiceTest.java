package com.ieee19.bc.interop.pf.proxy.ethereum;

import com.ieee19.bc.interop.pf.core.Utils;
import com.ieee19.bc.interop.pf.core.model.Block;
import com.ieee19.bc.interop.pf.proxy.ethereum.dto.rpc.JsonRpcRequest;
import com.ieee19.bc.interop.pf.proxy.ethereum.dto.rpc.JsonRpcTransaction;
import com.ieee19.bc.interop.pf.proxy.ethereum.dto.rpc.PendingTransactionResponse;
import com.ieee19.bc.interop.pf.proxy.ethereum.exception.EthereumException;
import com.ieee19.bc.interop.pf.proxy.ethereum.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGasPrice;

import java.io.IOException;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EthereumServiceTest {

    private static final String NODE_URL = "http://test.test";
    private static final String EXPECTED_ADDRESS = "db2a408b378b0d03e50b347526619f25086e8678";
    private static final String EXPECTED_TX_DATA1 = "aaa";
    private static final String EXPECTED_TX_DATA1_HEX = "0x616161";
    private static final String EXPECTED_TX_DATA2 = "bbb";
    private static final String EXPECTED_TX_DATA2_HEX = "0x626262";
    private static final String EXPECTED_TX_DATA3 = "kkk";
    private static final String EXPECTED_TX_DATA3_HEX = "0x6b6b6b";
    private static final String EXPECTED_TX_DATA4 = "xxxx";
    private static final String EXPECTED_TX_DATA4_HEX = "0x78787878";
    private static final long EXPECTED_BLOCK_NUMBER = 2;
    private static final long EXPECTED_GAS_LIMIT = 100000;
    private static final long EXPECTED_GAS_PRICE = 5000000000L;
    private static final String EXPECTED_BLOCK_HASH = "0x39e52715f3a078761f6668dcd793080f5b03fdcf521498aad7ff215533dbc9a2";
    private static final long EXPECTED_BLOCK_DIFFICTULTY = 5000000L;

    private EthereumService ethereumService;
    private RestTemplate restTemplateMock;
    private Web3j web3Mock;

    private PendingTransactionResponse createPendingTxResponse() {
        PendingTransactionResponse pendingTxResponse = new PendingTransactionResponse();

        JsonRpcTransaction tx = new JsonRpcTransaction();
        tx.setFromAddress(EXPECTED_ADDRESS);
        tx.setData(EXPECTED_TX_DATA1_HEX);
        pendingTxResponse.getPendingTransactions().add(tx);

        tx = new JsonRpcTransaction();
        tx.setFromAddress(EXPECTED_ADDRESS);
        tx.setData(EXPECTED_TX_DATA2_HEX);
        pendingTxResponse.getPendingTransactions().add(tx);

        tx = new JsonRpcTransaction();
        tx.setFromAddress("not from us");
        tx.setData(EXPECTED_TX_DATA3_HEX);
        pendingTxResponse.getPendingTransactions().add(tx);

        return pendingTxResponse;
    }

    @BeforeEach
    public void setUp() {
        restTemplateMock = mock(RestTemplate.class);
        Credentials credentials = mock(Credentials.class);
        when(credentials.getAddress()).thenReturn(EXPECTED_ADDRESS);
        ethereumService = new EthereumService(NODE_URL);
        ethereumService.setRestTemplate(restTemplateMock);
        ethereumService.setAccountCredentials(credentials);
        web3Mock = mock(Web3j.class);
        ethereumService.setWeb3(web3Mock);
        ethereumService.setThreadPoolSize(1);
    }

    @Test
    public void testGetPendingTransactions_shouldReturnCorrectResult() throws EthereumException {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest();
        jsonRpcRequest.setId(1);
        jsonRpcRequest.setMethod("parity_pendingTransactions");
        jsonRpcRequest.setJsonrpc("2.0");
        HttpEntity<JsonRpcRequest> request = new HttpEntity<>(jsonRpcRequest);
        PendingTransactionResponse expectedPendingTxResponse = createPendingTxResponse();

        when(restTemplateMock.postForObject(NODE_URL, request, PendingTransactionResponse.class))
                .thenReturn(expectedPendingTxResponse);

        List<Transaction> actualPendingTxs = ethereumService.getPendingTransactions();
        assertEquals(2, actualPendingTxs.size(), "Two pending txs are expected!");
        verify(restTemplateMock).postForObject(NODE_URL, request, PendingTransactionResponse.class);
    }

    @Test
    public void testGetPendingTransactions_shouldThrowException() throws EthereumException {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest();
        jsonRpcRequest.setId(1);
        jsonRpcRequest.setMethod("parity_pendingTransactions");
        jsonRpcRequest.setJsonrpc("2.0");
        HttpEntity<JsonRpcRequest> request = new HttpEntity<>(jsonRpcRequest);

        when(restTemplateMock.postForObject(NODE_URL, request, PendingTransactionResponse.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(EthereumException.class, () -> ethereumService.getPendingTransactions());
        verify(restTemplateMock).postForObject(NODE_URL, request, PendingTransactionResponse.class);
    }

    @Test
    public void testGetData_shouldReturnCorrectResult() throws EthereumException, IOException {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest();
        jsonRpcRequest.setId(1);
        jsonRpcRequest.setMethod("parity_pendingTransactions");
        jsonRpcRequest.setJsonrpc("2.0");
        HttpEntity<JsonRpcRequest> request = new HttpEntity<>(jsonRpcRequest);
        PendingTransactionResponse expectedPendingTxResponse = createPendingTxResponse();

        when(restTemplateMock.postForObject(NODE_URL, request, PendingTransactionResponse.class))
                .thenReturn(expectedPendingTxResponse);

        EthBlockNumber blockNumber = new EthBlockNumber();
        blockNumber.setResult("0x" + Long.toHexString(EXPECTED_BLOCK_NUMBER));
        Request<?, EthBlockNumber> blockNumberRequest = mock(Request.class);
        when(blockNumberRequest.send()).thenReturn(blockNumber);
        when(web3Mock.ethBlockNumber()).thenReturn((Request) blockNumberRequest);

        // new block (actual date)
        EthBlock expectedBlock = new EthBlock();
        expectedBlock.setError(null);
        EthBlock.TransactionResult txResult = new EthBlock.TransactionObject();
        ((EthBlock.TransactionObject) txResult).setFrom(EXPECTED_ADDRESS);
        ((EthBlock.TransactionObject) txResult).setInput(EXPECTED_TX_DATA4_HEX);
        EthBlock.Block block = new EthBlock.Block();
        block.setTimestamp("0x" + Long.toHexString(System.currentTimeMillis() / 1000));
        block.setTransactions(Arrays.asList(txResult));
        expectedBlock.setResult(block);

        Request<?, EthBlock> blockRequestYoung = mock(Request.class);
        when(blockRequestYoung.send()).thenReturn(expectedBlock);

        // old block (actual date minus 1 day)
        expectedBlock = new EthBlock();
        expectedBlock.setError(null);
        block = new EthBlock.Block();
        block.setTimestamp("0x" + Long.toHexString((System.currentTimeMillis() / 1000) - (3600*24)));  // minus 1 day
        expectedBlock.setResult(block);
        Request<?, EthBlock> blockRequestOld = mock(Request.class);
        when(blockRequestOld.send()).thenReturn(expectedBlock);


        when(web3Mock.ethGetBlockByNumber(any(DefaultBlockParameterNumber.class), anyBoolean()))
        .thenAnswer((invocation -> {
            BigInteger blockNumberParam = ((DefaultBlockParameterNumber)invocation.getArgument(0)).getBlockNumber();
            int blockNr = blockNumberParam.intValue();
            if (blockNr == EXPECTED_BLOCK_NUMBER) {
                return blockRequestYoung;
            }
            return blockRequestOld;
        }));


        List<String> actualData = ethereumService
                .getData(ZonedDateTime.now().minus(1, HOURS), ZonedDateTime.now().plus(1, HOURS));
        assertEquals(Arrays.asList(EXPECTED_TX_DATA1, EXPECTED_TX_DATA2, EXPECTED_TX_DATA4), actualData, "Data not matching!");
    }

    @Test
    public void testGetCurrentBlockNumber_shouldReturnCorrectResult() throws EthereumException, IOException {
        EthBlockNumber blockNumber = new EthBlockNumber();
        blockNumber.setResult("0x" + Long.toHexString(EXPECTED_BLOCK_NUMBER));
        Request<?, EthBlockNumber> blockNumberRequest = mock(Request.class);
        when(blockNumberRequest.send()).thenReturn(blockNumber);
        when(web3Mock.ethBlockNumber()).thenReturn((Request) blockNumberRequest);

        long actualBlockNumber = ethereumService.getCurrentBlockNumber();
        assertEquals(EXPECTED_BLOCK_NUMBER, actualBlockNumber, "Block numbers must match!");
    }

    @Test
    public void testGetMaxTransactionDataSizeInBytes_shouldReturnCorrectResult() throws IOException, EthereumException {
        EthBlock expectedBlock = new EthBlock();
        expectedBlock.setError(null);
        EthBlock.Block block = new EthBlock.Block();
        block.setGasLimit("0x" + Long.toHexString(EXPECTED_GAS_LIMIT));
        expectedBlock.setResult(block);

        Request<?, EthBlock> blockRequest = mock(Request.class);
        when(web3Mock.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false))
                .thenReturn((Request) blockRequest);
        when(blockRequest.send()).thenReturn(expectedBlock);

        int expectedSize = (int) Math.floor((EXPECTED_GAS_LIMIT - 21000)/68);
        int actualSize = ethereumService.getMaxTransactionDataSizeInBytes();
        assertEquals(expectedSize, actualSize, "Size values must match!");
    }

    @Test
    public void testGetGasPrice_shouldReturnCorrectResult() throws IOException, EthereumException {
        EthGasPrice ethGasPrice = new EthGasPrice();
        ethGasPrice.setError(null);
        ethGasPrice.setResult("0x" + Long.toHexString(EXPECTED_GAS_PRICE));

        Request<?, EthGasPrice> gasPriceRequest = mock(Request.class);

        when(web3Mock.ethGasPrice()).thenReturn((Request) gasPriceRequest);
        when(gasPriceRequest.send()).thenReturn(ethGasPrice);

        long actualGasPrice = ethereumService.getGasPrice();
        assertEquals(EXPECTED_GAS_PRICE, actualGasPrice, "Gas prices must match!");
    }

    @Test
    public void testGetBlockByNumberWithUncles_shouldReturnCorrectResult() throws IOException, EthereumException {
        long expectedTimestampInUnix = System.currentTimeMillis()/1000;
        ZonedDateTime expectedTimestamp = Utils.convertUnixEpochTime(expectedTimestampInUnix);
        EthBlock expectedBlock = new EthBlock();
        expectedBlock.setError(null);
        EthBlock.TransactionResult txResult = new EthBlock.TransactionObject();
        ((EthBlock.TransactionObject) txResult).setFrom(EXPECTED_ADDRESS);
        ((EthBlock.TransactionObject) txResult).setInput(EXPECTED_TX_DATA4_HEX);
        EthBlock.Block block = new EthBlock.Block();
        block.setNumber("0x" + Long.toHexString(EXPECTED_BLOCK_NUMBER));
        block.setHash(EXPECTED_BLOCK_HASH);
        block.setParentHash(EXPECTED_BLOCK_HASH);
        block.setTransactions(Arrays.asList(txResult));
        block.setUncles(Collections.emptyList());
        block.setTimestamp("0x" + Long.toHexString(expectedTimestampInUnix));
        block.setDifficulty("0x" + Long.toHexString(EXPECTED_BLOCK_DIFFICTULTY));
        expectedBlock.setResult(block);

        Request<?, EthBlock> blockRequest = mock(Request.class);
        when(blockRequest.send()).thenReturn(expectedBlock);
        when(web3Mock.ethGetBlockByNumber(any(DefaultBlockParameterNumber.class), anyBoolean()))
                .thenAnswer(invocation -> {
                    Long blockNumber = ((DefaultBlockParameterNumber)invocation.getArgument(0)).getBlockNumber().longValue();
                    if (blockNumber.equals(EXPECTED_BLOCK_NUMBER)) {
                        return blockRequest;
                    }
                    return null;
                });

        Optional<Block> actualBlock = ethereumService.getBlockByNumberWithUncles(EXPECTED_BLOCK_NUMBER);
        assertTrue(actualBlock.isPresent(), "No block present!");
        assertEquals(EXPECTED_BLOCK_HASH, actualBlock.get().getHash(), "Block hash not matching!");
        assertEquals(EXPECTED_BLOCK_HASH, actualBlock.get().getPreviousBlockHash(), "Prev hash not matching!");
        assertEquals(EXPECTED_BLOCK_NUMBER, actualBlock.get().getHeight(), "Block number not matching!");
        assertEquals(EXPECTED_BLOCK_NUMBER, actualBlock.get().getHeight(), "Block number not matching!");
        assertEquals(block.getTransactions().size(), actualBlock.get().getNumberOfTransactions(), "Tx number not matching!");
        assertEquals(block.getUncles().size(), actualBlock.get().getUncleBlocks().size(), "Uncles not matching!");
        assertEquals(EXPECTED_BLOCK_DIFFICTULTY, actualBlock.get().getDifficulty(), "Difficulty not matching!");
        assertEquals(expectedTimestamp, actualBlock.get().getTimestamp(), "Timestamp not matching!");
    }

}
