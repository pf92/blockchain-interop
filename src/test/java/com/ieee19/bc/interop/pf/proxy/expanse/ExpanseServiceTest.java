package com.ieee19.bc.interop.pf.proxy.expanse;

import com.ieee19.bc.interop.pf.proxy.ethereum.EthereumService;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExpanseServiceTest {

    private static final String NODE_URL = "http://test.test";
    private static final String EXPECTED_ADDRESS = "db2a408b378b0d03e50b347526619f25086e8678";
    private static final String EXPECTED_TX_DATA1 = "aaa";
    private static final String EXPECTED_TX_DATA1_HEX = "0x616161";
    private static final String EXPECTED_TX_DATA2 = "bbb";
    private static final String EXPECTED_TX_DATA2_HEX = "0x626262";

    private EthereumService ethereumService;
    private RestTemplate restTemplateMock;

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

        return pendingTxResponse;
    }

    @BeforeEach
    public void setUp() {
        restTemplateMock = mock(RestTemplate.class);
        Credentials credentials = mock(Credentials.class);
        when(credentials.getAddress()).thenReturn(EXPECTED_ADDRESS);
        ethereumService = new ExpanseService(NODE_URL);
        ethereumService.setRestTemplate(restTemplateMock);
        ethereumService.setAccountCredentials(credentials);
        ethereumService.setThreadPoolSize(1);
    }

    @Test
    public void testGetPendingTransactions_shouldReturnCorrectResult() throws EthereumException {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest();
        jsonRpcRequest.setId(0);
        jsonRpcRequest.setMethod("eth_pendingTransactions");
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
        jsonRpcRequest.setId(0);
        jsonRpcRequest.setMethod("eth_pendingTransactions");
        jsonRpcRequest.setJsonrpc("2.0");
        HttpEntity<JsonRpcRequest> request = new HttpEntity<>(jsonRpcRequest);

        when(restTemplateMock.postForObject(NODE_URL, request, PendingTransactionResponse.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(EthereumException.class, () -> ethereumService.getPendingTransactions());
        verify(restTemplateMock).postForObject(NODE_URL, request, PendingTransactionResponse.class);
    }


}
