package com.ieee19.bc.interop.pf.proxy.expanse;

import com.ieee19.bc.interop.pf.proxy.ethereum.DtoConverter;
import com.ieee19.bc.interop.pf.proxy.ethereum.EthereumService;
import com.ieee19.bc.interop.pf.proxy.ethereum.dto.rpc.JsonRpcRequest;
import com.ieee19.bc.interop.pf.proxy.ethereum.dto.rpc.PendingTransactionResponse;
import com.ieee19.bc.interop.pf.proxy.ethereum.exception.EthereumException;
import com.ieee19.bc.interop.pf.proxy.ethereum.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.web3j.crypto.Credentials;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class provides methods for interacting with the Expanse network.
 *
 */
public class ExpanseService extends EthereumService {

    private static final Logger LOG = LoggerFactory.getLogger(ExpanseService.class);

    /**
     *
     * @param nodeBaseUrl the URL of a gexp node
     * @param accountCredentials the credentials of the account that should be used for sending transactions
     * @throws EthereumException
     */
    public ExpanseService(String nodeBaseUrl, Credentials accountCredentials) throws EthereumException {
        super(nodeBaseUrl, accountCredentials);
    }

    public ExpanseService(String nodeBaseUrl) {
        super(nodeBaseUrl);
    }

    @Override
    public List<Transaction> getPendingTransactions() throws EthereumException {
        // only works if a gexp node is used (pending transaction sent by one of the gexp accounts are returned)
        LOG.info("Get pending transactions sent by addr");
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest();
        jsonRpcRequest.setId(0);
        jsonRpcRequest.setMethod("eth_pendingTransactions");
        jsonRpcRequest.setJsonrpc("2.0");
        HttpEntity<JsonRpcRequest> request = new HttpEntity<>(jsonRpcRequest);
        PendingTransactionResponse response = doPOST(getNodeBaseUrl(), request, PendingTransactionResponse.class);

        List<Transaction> pendingTransactions = response
                .getPendingTransactions()
                .parallelStream()
                .map(DtoConverter::convert)
                .collect(Collectors.toList());

        LOG.info("Pending transactions found: " + pendingTransactions.size());
        return pendingTransactions;
    }

}
