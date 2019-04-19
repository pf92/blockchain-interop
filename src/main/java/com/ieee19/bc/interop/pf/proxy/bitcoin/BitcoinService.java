package com.ieee19.bc.interop.pf.proxy.bitcoin;

import com.ieee19.bc.interop.pf.core.model.Block;
import com.ieee19.bc.interop.pf.core.Utils;
import com.ieee19.bc.interop.pf.proxy.bitcoin.dto.bitcore.*;
import com.ieee19.bc.interop.pf.proxy.bitcoin.dto.blockcypher.FeePerKbInfo;
import com.ieee19.bc.interop.pf.proxy.bitcoin.exception.BitcoinException;
import com.ieee19.bc.interop.pf.proxy.bitcoin.interfaces.IBitcoinService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import sd.fomin.gerbera.transaction.Transaction;
import sd.fomin.gerbera.transaction.TransactionBuilder;
import sd.fomin.gerbera.util.HexUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class provides methods for interacting with the Bitcoin network.
 */
public class BitcoinService implements IBitcoinService {

    private static final Logger LOG = LoggerFactory.getLogger(BitcoinService.class);
    private static final String BLOCKCYPHER_BASE_URL = "https://api.blockcypher.com/v1/btc/main/";
    private static final String BLOCKCHAIN_INFO_BASE_URL = "https://blockchain.info/";
    private static final int TX_DATA_MAX_LENGTH = 40;  // 40 Bytes

    private RestTemplate restTemplate = new RestTemplate();
    private String address;
    private String privateKeyWif; // Wallet Import Format is a way of encoding a private ECDSA key
    private String bitcoreNodeUrl;

    /**
     * @param nodeUrl       the URL of the Bitcore node (insight API)
     *      * @param address       the adress that should be used for signing and submitting tansactions
     *      * @param privateKeyWif the corresponding private key in WIF
     */
    public BitcoinService(String nodeUrl, String address, String privateKeyWif) {
        this.bitcoreNodeUrl = nodeUrl;
        this.address = address;
        this.privateKeyWif = privateKeyWif;
    }

    /**
     * This constructor can be used if the {@link BitcoinService} is not used for reading data from or writing data to the
     * blockchain.
     *
     * @param nodeUrl the URL of the Bitcore node (insight API)
     */
    public BitcoinService(String nodeUrl) {
        this.bitcoreNodeUrl = nodeUrl;
    }

    private Block convert(BitcoinBlock bitcoinBlock) {
        Block block = new Block();

        block.setDifficulty(bitcoinBlock.getDifficulty());
        block.setNumberOfTransactions(bitcoinBlock.getTransactionHashes().size());
        block.setHash(bitcoinBlock.getHash());
        block.setHeight(bitcoinBlock.getBlockNumber());
        block.setPreviousBlockHash(bitcoinBlock.getPreviousBlockHash());
        block.setTimestamp(Utils.convertUnixEpochTime(bitcoinBlock.getTimeInSeconds()));

        return block;
    }

    private <T> T doGET(String url, Class<T> responseType) throws BitcoinException {
        T response;

        try {
            response = restTemplate.getForObject(url, responseType);
        } catch (RestClientException e) {
            LOG.error("Failed to call GET " + url, e);
            throw new BitcoinException("Failed to call GET " + url, e);
        }

        return response;
    }

    private <T> T doPOST(String url, Object request, Class<T> responseType) throws BitcoinException {
        T response;

        try {
            response = restTemplate.postForObject(url, request, responseType);
        } catch (RestClientException e) {
            LOG.error("Failed to call POST " + url, e);
            throw new BitcoinException("Failed to call POST " + url, e);
        }

        return response;
    }

    private String getBlockHashByBlockNumber(long blockNumber) throws BitcoinException {
        LOG.debug("get block hash by block number " + blockNumber);
        String response = doGET(bitcoreNodeUrl + "block-index/" + blockNumber, String.class);
        LOG.debug("Response: " + response);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> responseJson = null;
        try {
            responseJson = mapper.readValue(response, new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new BitcoinException(e.getMessage(), e);
        }
        String blockHash = responseJson.get("blockHash");
        LOG.debug("block hash: " + blockHash);

        return blockHash;
    }

    private BitcoinTransaction getTransactionByHash(String hash) throws BitcoinException {
        LOG.debug("get transaction by hash (txid) " + hash);
        BitcoinTransaction bitcoinTransaction = doGET(bitcoreNodeUrl + "tx/" + hash,
                BitcoinTransaction.class);
        LOG.debug("Retrieved: " + bitcoinTransaction);

        return bitcoinTransaction;
    }

    private Optional<String> getMinerAddressFromBitcore(String transactionHash) throws BitcoinException {
        LOG.debug("get miner address of transaction " + transactionHash);
        BitcoinTransaction coinBaseTransaction = getTransactionByHash(transactionHash);
        Optional<String> minerAddress = Optional.empty();
        List<BitcoinTransactionOutput> transactionOutputs = coinBaseTransaction.getTransactionOutputs();
        if (coinBaseTransaction.isCoinBase() && transactionOutputs.isEmpty() == false) {
            BitcoinTransactionOutput transactionOutput = transactionOutputs.get(0);  // first output should contain addr.
            List<String> addresses = transactionOutput.getScriptPubKey().getAddresses();
            if (addresses.isEmpty() == false) {
                minerAddress = Optional.of(addresses.get(0));
            }
        }
        LOG.debug("minerAddress: " + minerAddress);
        return minerAddress;
    }

    private Optional<String> getMinerAddressFromBlockchainInfo(String transactionHash) {
        LOG.debug("get miner address for transaction " + transactionHash + " from blockchain.info");
        try {
            com.ieee19.bc.interop.pf.proxy.bitcoin.dto.blockchaininfo.BitcoinTransaction transaction =
                    doGET(
                            BLOCKCHAIN_INFO_BASE_URL + "rawtx/" + transactionHash,
                            com.ieee19.bc.interop.pf.proxy.bitcoin.dto.blockchaininfo.BitcoinTransaction.class
                    );

            if (transaction.getTransactionOutputs().isEmpty() == false) {
                com.ieee19.bc.interop.pf.proxy.bitcoin.dto.blockchaininfo.BitcoinTransactionOutput transactionOutput =
                        transaction.getTransactionOutputs().get(0);
                String transactionOutputAddress = transactionOutput.getAddress();
                if (transactionOutputAddress != null && "".equals(transactionOutputAddress) == false) {
                    // not null and not an empty string
                    LOG.debug("miner address: " + transactionOutputAddress);
                    return Optional.of(transactionOutputAddress);
                }
            }
        } catch (BitcoinException e) {
            LOG.warn(e.getMessage(), e);
        }
        LOG.debug("no miner address found");
        return Optional.empty();
    }

    private String getMinerAddress(String transactionHash) throws BitcoinException {
        Optional<String> minerAddress = getMinerAddressFromBitcore(transactionHash);
        if (minerAddress.isPresent()) {
            return minerAddress.get();
        }

        // try blockchain.info api
        minerAddress = getMinerAddressFromBlockchainInfo(transactionHash);
        if (minerAddress.isPresent()) {
            return minerAddress.get();
        }
        return "Unknown miner";
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public long getCurrentBlockHeight() throws BitcoinException {
        LOG.debug("get current block height");
        BitcoinInfo bitcoinInfo = doGET(bitcoreNodeUrl + "status?q=getInfo",
                BitcoinInfo.class);
        LOG.debug("Retrieved: " + bitcoinInfo);
        return bitcoinInfo.getCurrentBlockHeight();
    }

    @Override
    public Block getBlockByBlockNumber(long blockNumber) throws BitcoinException {
        LOG.debug("get block by block number " + blockNumber);
        String blockHash = getBlockHashByBlockNumber(blockNumber);
        BitcoinBlock bitcoinBlock = doGET(bitcoreNodeUrl + "block/" + blockHash,
                BitcoinBlock.class);
        Block block = convert(bitcoinBlock);

        block.setMinerAddress(getMinerAddress(bitcoinBlock.getTransactionHashes().get(0)));

        LOG.debug("Retrieved: " + block);
        return block;
    }

    @Override
    public FeePerKbInfo getFeePerKbInfo() throws BitcoinException {
        LOG.debug("get fee info");
        FeePerKbInfo feePerKbInfo = doGET(BLOCKCYPHER_BASE_URL, FeePerKbInfo.class);
        LOG.debug("Retrieved: " + feePerKbInfo);
        return feePerKbInfo;
    }

    @Override
    public List<String> getData(ZonedDateTime from, ZonedDateTime to) throws BitcoinException {
        LOG.info("get data between " + from + " and " + to);
        BitcoinAddressTransactions addressTransactions = doGET(
                bitcoreNodeUrl + "txs?address=" + address,
                BitcoinAddressTransactions.class
        );
        List<BitcoinTransaction> transactions = addressTransactions.getTransactions();
        List<String> data = new ArrayList<>();

        for (BitcoinTransaction transaction : transactions) {
            ZonedDateTime txTime = Utils.convertUnixEpochTime(transaction.getBlockTime());
            if ((txTime.isAfter(from) && txTime.isBefore(to)) || transaction.getConfirmations() == 0) {
                List<BitcoinTransactionOutput> txOutputs = transaction.getTransactionOutputs();
                List<String> dataEntries = txOutputs
                        .parallelStream()
                        .map(txOutput -> txOutput.getScriptPubKey().getHex())
                        .filter(hex -> hex.startsWith("6a"))  // Opcode for OP_RETURN
                        .map(hex -> hex.substring(4))  // OpCode 2 Chars, Length 2 Chars -> Data starts com idx 4
                        .map(hex -> {
                            String dataStr = "";
                            try {
                                dataStr = Utils.decodeHex(hex);
                            } catch (DecoderException | UnsupportedEncodingException e) {
                                LOG.error(e.getMessage(), e);
                            }
                            return dataStr;
                        })
                        .collect(Collectors.toList());
                data.addAll(dataEntries);
            }
        }
        LOG.info("data: " + Arrays.toString(data.toArray()));
        return data;
    }

    @Override
    public void storeData(String dataString, long txFeesInSatoshi) throws BitcoinException {
        LOG.info("Write " + dataString.getBytes().length + " bytes to bitcoin: " + dataString);
        if (dataString.getBytes().length > TX_DATA_MAX_LENGTH) {
            throw new BitcoinException("Data string exceeds max. length of " + TX_DATA_MAX_LENGTH + " bytes!");
        }
        broadcastTransaction(createRawTransaction(dataString, txFeesInSatoshi));
    }

    private String broadcastTransaction(String rawTransaction) throws BitcoinException {
        LOG.info("broadcast rawTransaction " + rawTransaction);
        BroadcastTransactionRequest request = new BroadcastTransactionRequest();
        request.setRawTransaction(rawTransaction);
        BroadcastTransactionResponse response = doPOST(
                bitcoreNodeUrl + "tx/send",
                request,
                BroadcastTransactionResponse.class
        );

        LOG.info("Sent transaction with id: " + response.getTansactionId());

        return response.getTansactionId();
    }

    private String createRawTransaction(String data, long txFeesInSatoshi) throws BitcoinException {
        LOG.info("create raw transaction for data " + data);
        List<BitcoinUnspentTransactionOutput> utxOutputs = getUnspentTxOutputs();
        List<BitcoinUnspentTransactionOutput> requiredUTxOutputs = getEnoughUnspentTxOutputsFor(
                txFeesInSatoshi + 1,
                utxOutputs
        );

        if (requiredUTxOutputs.isEmpty()) {
            throw new BitcoinException("No suitable unspent transaction outputs found for address " + address);
        }

        TransactionBuilder txBuilder = TransactionBuilder.create(false);
        for (BitcoinUnspentTransactionOutput utxo : requiredUTxOutputs) {
            txBuilder
                    .from(
                            utxo.getTransactionId(),
                            utxo.getTxOutputNumber(),
                            utxo.getScriptPubKey(),
                            utxo.getSatoshis(),
                            privateKeyWif
                    );
        }
        Transaction transaction = txBuilder
                .put(HexUtils.asString(data.getBytes()), 1)
                .withFee(txFeesInSatoshi)
                .changeTo(address)
                .build();

        String rawTransaction = transaction.getRawTransaction();
        LOG.info("Raw JsonRpcTransaction: " + rawTransaction);
        return rawTransaction;
    }

    private List<BitcoinUnspentTransactionOutput> getEnoughUnspentTxOutputsFor(long satoshis,
                                                                               List<BitcoinUnspentTransactionOutput> unspentTxOutputs) {
        LOG.info("get enough unspent tx outputs for " + satoshis + " satoshis and utxos: " + Arrays.toString(unspentTxOutputs.toArray()));
        List<BitcoinUnspentTransactionOutput> result = new ArrayList<>();
        int sum = 0;

        for (BitcoinUnspentTransactionOutput unspentTxOutput : unspentTxOutputs) {
            if (sum >= satoshis) {
                break;
            }
            result.add(unspentTxOutput);
            sum += unspentTxOutput.getSatoshis();
        }

        LOG.info("result: " + Arrays.toString(result.toArray()));
        return result;
    }

    private List<BitcoinUnspentTransactionOutput> getUnspentTxOutputs() throws BitcoinException {
        LOG.info("get unspent transaction outputs for address " + address);
        BitcoinUnspentTransactionOutput[] utxOutputs = doGET(
                bitcoreNodeUrl + "addr/" + address + "/utxo",
                BitcoinUnspentTransactionOutput[].class);
        List<BitcoinUnspentTransactionOutput> utxOutputList = new ArrayList<>();

        if (utxOutputs != null && utxOutputs.length > 0) {
            utxOutputList = Arrays.asList(utxOutputs);
        } else {
            LOG.warn("No utxo retrieved!");
        }

        LOG.info("Retrieved: " + Arrays.toString(utxOutputList.toArray()));
        return utxOutputList;
    }

}
