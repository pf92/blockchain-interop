package com.ieee19.bc.interop.pf.proxy.ethereum;

import com.ieee19.bc.interop.pf.core.model.Block;
import com.ieee19.bc.interop.pf.core.Utils;
import com.ieee19.bc.interop.pf.proxy.ethereum.dto.rpc.JsonRpcRequest;
import com.ieee19.bc.interop.pf.proxy.ethereum.dto.rpc.PendingTransactionResponse;
import com.ieee19.bc.interop.pf.proxy.ethereum.exception.EthereumException;
import com.ieee19.bc.interop.pf.proxy.ethereum.interfaces.IEthereumService;
import com.ieee19.bc.interop.pf.proxy.ethereum.model.Transaction;
import org.apache.commons.codec.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * This class provides methods for interacting with the Ethereum network.
 *
 */
public class EthereumService implements IEthereumService {

    private static final Logger LOG = LoggerFactory.getLogger(EthereumService.class);

    protected Web3j web3;
    protected RestTemplate restTemplate = new RestTemplate();
    private String nodeBaseUrl;
    private Credentials accountCredentials;
    private AtomicLong currentNonce = new AtomicLong(0);
    private int threadPoolSize = 25;

    /**
     * @param nodeBaseUrl        the URL of the Parity node
     * @param accountCredentials the credentials of the account that should be used for sending transactions
     * @throws EthereumException
     */
    public EthereumService(String nodeBaseUrl, Credentials accountCredentials) throws EthereumException {
        this.nodeBaseUrl = nodeBaseUrl;
        this.accountCredentials = accountCredentials;
        web3 = Web3j.build(new HttpService(nodeBaseUrl));
        currentNonce.set(getNonceFromNetwork());
    }

    /**
     * This constructor can be used if the {@link EthereumService} not used for reading data from or writing data to the
     * blockchain.
     *
     * @param nodeBaseUrl the URL of the Parity node
     */
    public EthereumService(String nodeBaseUrl) {
        this.nodeBaseUrl = nodeBaseUrl;
        web3 = Web3j.build(new HttpService(nodeBaseUrl));
    }

    private long getNonceFromNetwork() throws EthereumException {
        try {
            EthGetTransactionCount response = web3
                    .ethGetTransactionCount(accountCredentials.getAddress(), DefaultBlockParameterName.LATEST)
                    .send();
            if (response.hasError()) {
                throw new EthereumException("Could not get nonce (txCount): " + response.getError().getMessage());
            }
            return response.getTransactionCount().longValue();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new EthereumException("Could not retrieve nonce", e);
        }
    }

    private List<Block> getUncleBlocks(EthBlock.Block ethBlock) throws EthereumException {
        List<Block> uncleBlocks = new ArrayList<>();
        try {
            for (int i = 0; i < ethBlock.getUncles().size(); i++) {

                EthBlock response = web3
                        .ethGetUncleByBlockHashAndIndex(ethBlock.getHash(), BigInteger.valueOf(i))
                        .send();
                if (response.hasError()) {
                    throw new EthereumException("Couldn't get uncle blocks for block " + ethBlock.getHash() + ": " +
                            response.getError().getMessage());
                }
                uncleBlocks.add(DtoConverter.convert(response.getBlock()));
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new EthereumException(e.getMessage(), e);
        }

        return uncleBlocks;
    }

    private long getCurrentGasLimit() throws EthereumException {
        try {
            EthBlock ethBlock = web3
                    .ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false)
                    .send();
            if (ethBlock.hasError()) {
                LOG.error("Got Error from API: " + ethBlock.getError().getMessage());
                throw new EthereumException("Failed to get gas limit");
            }
            return ethBlock.getBlock().getGasLimit().longValue();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new EthereumException("Failed to get gas limit", e);
        }
    }

    protected <T> T doPOST(String url, Object request, Class<T> responseType) throws EthereumException {
        T response;

        try {
            response = restTemplate.postForObject(url, request, responseType);
        } catch (RestClientException e) {
            LOG.error("Failed to call POST " + url, e);
            throw new EthereumException("Failed to call POST " + url, e);
        }

        return response;
    }

    protected String getNodeBaseUrl() {
        return nodeBaseUrl;
    }

    private List<Transaction> getMinedTransactionsSentByAddr(ZonedDateTime from, ZonedDateTime to) throws EthereumException {
        List<Transaction> transactions = new ArrayList<>();
        LOG.info("Get all mined transactions sent between " + from + " and " + to);
        AtomicLong nextBlockHeightToFetch = new AtomicLong(getCurrentBlockNumber());
        Callable<List<Transaction>> fetchTxListOfBlockTask = () -> {
            long nextBlockNumber;
            ZonedDateTime blockTime = null;
            List<Transaction> txs = new ArrayList<>();
            do {
                nextBlockNumber = nextBlockHeightToFetch.getAndDecrement();
                LOG.debug("Next block height: " + nextBlockNumber);
                EthBlock blockResponse = null;

                // TODO
                boolean repeat;
                do {
                    LOG.info("next block: " + nextBlockNumber);
                    try {
                        blockResponse = web3
                                .ethGetBlockByNumber(new DefaultBlockParameterNumber(nextBlockNumber), true)
                                .send();
                        repeat = false;
                    } catch (IOException e) {
                        LOG.error(e.getMessage());  // TODO
                        repeat = true;
                    }
                }
                while (repeat);

                if (blockResponse.hasError()) {
                    LOG.error("Failed to retrieve Ethereum block " + nextBlockNumber + ": " +
                            blockResponse.getError().getMessage());
                    throw new EthereumException("Failed to retrieve Ethereum block " + nextBlockNumber + ": " +
                            blockResponse.getError().getMessage());
                }
                EthBlock.Block block = blockResponse.getBlock();

                if (block == null) { // TODO
                    LOG.error("null");
                    blockTime = to;
                    continue;
                }

//                LOG.info("block" + block.getNumber());
                blockTime = Utils.convertUnixEpochTime(block.getTimestamp().longValue());
                LOG.info("blockTime: " + blockTime);

                if (blockTime.isAfter(from) && blockTime.isBefore(to)) {
                    // check if there are transactions sent by the services' address
                    for (EthBlock.TransactionResult txResult : blockResponse.getBlock().getTransactions()) {
                        org.web3j.protocol.core.methods.response.Transaction tx =
                                (org.web3j.protocol.core.methods.response.Transaction) txResult.get();

                        if (tx.getFrom().equals(accountCredentials.getAddress())) {
                            txs.add(DtoConverter.convert(tx));
                        }
                    }
                }

            } while (blockTime.isAfter(from));

            return txs;
        };
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        List<Future<List<Transaction>>> futures = new ArrayList<>(threadPoolSize);

        // submit tasks
        for (int i = 0; i < threadPoolSize; i++) {
            futures.add(executorService.submit(fetchTxListOfBlockTask));
        }

        executorService.shutdown();
        while (!executorService.isTerminated()) {
        }

        try {
            for (Future<List<Transaction>> future : futures) {
                transactions.addAll(future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Failed to get mined transactions sent by addr", e);
            throw new EthereumException("Failed to get mined transactions sent by addr", e);
        }

        LOG.info("Mined transactions sent by addr: " + transactions.size());
        return transactions;
    }

    public void setWeb3(Web3j web3) {
        this.web3 = web3;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public void setAccountCredentials(Credentials accountCredentials) {
        this.accountCredentials = accountCredentials;
    }

    public String getAccountAddress() {
        return accountCredentials.getAddress();
    }

    public List<Transaction> getPendingTransactions() throws EthereumException {
        // only works if a rpc node is used
        LOG.info("Get pending transactions sent by addr");
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest();
        jsonRpcRequest.setId(1);
        jsonRpcRequest.setMethod("parity_pendingTransactions");
        jsonRpcRequest.setJsonrpc("2.0");
        HttpEntity<JsonRpcRequest> request = new HttpEntity<>(jsonRpcRequest);
        PendingTransactionResponse response = doPOST(nodeBaseUrl, request, PendingTransactionResponse.class);

        LOG.info("Overall found pending transactions: " + response.getPendingTransactions().size());
        List<Transaction> pendingTransactions = response
                .getPendingTransactions()
                .parallelStream()
                .filter(tx -> tx.getFromAddress().equals(accountCredentials.getAddress()))
                .map(DtoConverter::convert)
                .collect(Collectors.toList());

        LOG.info("Pending transactions sent by this addr: " + pendingTransactions.size());
        return pendingTransactions;
    }

    @Override
    public int getMaxTransactionDataSizeInBytes() throws EthereumException {
        int txGas = 21000;  // has to be paid for every transaction
        int gasForNonZeroByte = 68;     // has to be paid for every non-zero byte
        long currentGasLimit = getCurrentGasLimit();

        return (int) Math.floor((currentGasLimit - txGas) / gasForNonZeroByte);
    }

    @Override
    public long getCurrentBlockNumber() throws EthereumException {
        EthBlockNumber response = null;
        try {
            response = web3
                    .ethBlockNumber()
                    .send();
            if (response.hasError()) {
                throw new EthereumException("Could not get current block number: " + response.getError().getMessage());
            }
            return response.getBlockNumber().longValue();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new EthereumException("Could not get current block number", e);
        }
    }

    @Override
    public Optional<Block> getBlockByNumberWithUncles(long blockNumber) throws EthereumException {
        LOG.debug("Get block with number " + blockNumber);
        EthBlock.Block ethBlock;
        Optional<Block> ret = Optional.empty();

        try {
            EthBlock response = web3
                    .ethGetBlockByNumber(new DefaultBlockParameterNumber(blockNumber), false)
                    .send();
            if (response.hasError()) {
                throw new EthereumException("Failed to get block with number " + blockNumber + ": " + response.getError().getMessage());
            }
            ethBlock = response.getBlock();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new EthereumException(e.getMessage(), e);
        }

        if (ethBlock != null) {
            Block block = DtoConverter.convert(ethBlock);
            block.setUncleBlocks(getUncleBlocks(ethBlock));
            ret = Optional.of(block);
        }

        LOG.debug("Retrieved block: " + (ret.isPresent() ? ret.get().toString() : "null"));
        return ret;
    }

    @Override
    public Long getGasPrice() throws EthereumException {
        try {
            EthGasPrice response = web3
                    .ethGasPrice()
                    .send();
            if (response.hasError()) {
                throw new EthereumException("Failed to get gas price: " + response.getError().getMessage());
            }
            return response.getGasPrice().longValue();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new EthereumException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> getData(ZonedDateTime from, ZonedDateTime to) throws EthereumException {
        List<String> dataStrings = new ArrayList<>();
        try {
            String txData;
            List<Transaction> transactions = getPendingTransactions();
            transactions.addAll(getMinedTransactionsSentByAddr(from, to));

            for (Transaction tx : transactions) {
                txData = Utils.decodeHex(tx.getData());
                if (txData.length() > 0) {
                    dataStrings.add(txData);
                }
            }
        } catch (DecoderException | UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
            throw new EthereumException("Failed to decode hex string");
        } catch (EthereumException e) {
            LOG.error(e.getMessage(), e);
            throw new EthereumException("Failed to get pending or mined transactions.", e);
        }
        return dataStrings;
    }

    @Override
    public void storeData(String dataStr, long gasPriceInWei) throws EthereumException {
        LOG.info("Store data: " + dataStr);
        int maxDataSizeInBytes = getMaxTransactionDataSizeInBytes();
        if (dataStr.getBytes().length > maxDataSizeInBytes) {
            throw new EthereumException("Data string exceeds max length of " + maxDataSizeInBytes + " bytes");
        }
//        createAndSendTxWithData(dataStr, gasPriceInWei);  // TODO
    }

    private void createAndSendTxWithData(String data, long gasPriceInWei) throws EthereumException {
        try {
            BigInteger nonce = BigInteger.valueOf(currentNonce.getAndIncrement());
            BigInteger gasLimit = BigInteger.valueOf(21001L + (data.getBytes().length * 68));  // 21000 for every tx, 68 for every non-zero byte
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    nonce,
                    BigInteger.valueOf(gasPriceInWei),  // gas price
                    gasLimit,         // gas limit
                    accountCredentials.getAddress(),  // to address (same as from address)
                    Utils.encodeHex(data.getBytes())
            );

            byte[] signedTx = TransactionEncoder.signMessage(rawTransaction, accountCredentials);
            String signedTxAsHex = Numeric.toHexString(signedTx);
            EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(signedTxAsHex).send();
            if (ethSendTransaction.hasError()) {
                LOG.error("Failed to send JsonRpcTransaction: " + ethSendTransaction.getError().getMessage());
                throw new EthereumException("Failed to send JsonRpcTransaction: " + ethSendTransaction.getError().getMessage());
            }
            String transactionHash = ethSendTransaction.getTransactionHash();
            LOG.info("Successfully sent transaction: " + transactionHash);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new EthereumException("Failed to send JsonRpcTransaction", e);
        }
    }

}
