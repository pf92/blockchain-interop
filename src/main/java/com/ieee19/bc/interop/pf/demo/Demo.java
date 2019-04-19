package com.ieee19.bc.interop.pf.demo;

import com.ieee19.bc.interop.pf.core.AbstractDataAccessService;
import com.ieee19.bc.interop.pf.core.BlockchainManager;
import com.ieee19.bc.interop.pf.core.model.BlockchainMetaData;
import com.ieee19.bc.interop.pf.core.model.MetricValidationSettings;
import com.ieee19.bc.interop.pf.core.model.ThresholdValidationSettings;
import com.ieee19.bc.interop.pf.core.model.WeightedRankingSettings;
import com.ieee19.bc.interop.pf.proxy.bitcoin.BitcoinDataAccessService;
import com.ieee19.bc.interop.pf.proxy.bitcoin.BitcoinMetricCollector;
import com.ieee19.bc.interop.pf.proxy.bitcoin.BitcoinService;
import com.ieee19.bc.interop.pf.proxy.currency.CryptocurrencyPriceService;
import com.ieee19.bc.interop.pf.proxy.currency.Currency;
import com.ieee19.bc.interop.pf.proxy.ethereum.AbstractEthereumMetricCollector;
import com.ieee19.bc.interop.pf.proxy.ethereum.EthereumDataAccessService;
import com.ieee19.bc.interop.pf.proxy.ethereum.EthereumMetricCollector;
import com.ieee19.bc.interop.pf.proxy.ethereum.EthereumService;
import com.ieee19.bc.interop.pf.proxy.ethereum.exception.EthereumException;
import com.ieee19.bc.interop.pf.proxy.ethereumclassic.EthereumClassicMetricCollector;
import com.ieee19.bc.interop.pf.proxy.expanse.ExpanseMetricCollector;
import com.ieee19.bc.interop.pf.proxy.expanse.ExpanseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class shows how to create a Blockchain Manager that uses Bitcoin, Ethereum, Ethereum Classic, and Expanse.
 */
public class Demo {

    private static Logger LOG = LoggerFactory.getLogger(Demo.class);

    // change these urls to the addresses of your nodes
    private static final String NODE_URL_BITCOIN_MAIN = "http://35.242.216.4:3001/insight-api/";
    private static final String NODE_URL_ETHEREUM_MAIN = "http://35.242.203.9:8545";
    private static final String NODE_URL_ETHEREUM_CLASSIC_MAIN = "http://35.234.102.37:8545";
    private static final String NODE_URL_EXPANSE_MAIN = "http://35.198.77.10:9656";

    private List<BlockchainMetaData> blockchainMetaDataList = new ArrayList<>();
    private BlockchainManager blockchainManager;

    public static void main(String[] args) throws EthereumException {
        Demo demo = new Demo();

        demo.createBlockchainMetadataList();
        demo.createBlockchainManager();
        demo.subscribeToSwitchoverSuggestions();
    }


    private void createBlockchainMetadataList() throws EthereumException {
        // Bitcoin
        BitcoinService bitcoinService = new BitcoinService(
                NODE_URL_BITCOIN_MAIN,
                "YourBitcoinAddress",        // fill in your Bitcoin address
                "YourPrivateKeyinWIF");  // specify your private key in Wallet Import Format (WIF)
        BitcoinMetricCollector bitcoinMetricCollector = new BitcoinMetricCollector(
                bitcoinService, new CryptocurrencyPriceService(), Currency.US_DOLLAR, 5
        );
        AbstractDataAccessService bitcoinDataAccessService = new BitcoinDataAccessService(
                (str, maxL) -> Arrays.asList(str), (lst, maxL) -> lst, bitcoinService
        );
        BlockchainMetaData bitcoin = new BlockchainMetaData();
        bitcoin.setIdentifier("Bitcoin");
        bitcoin.setMetricCollector(bitcoinMetricCollector);
        bitcoin.setDataAccessService(bitcoinDataAccessService);
        bitcoin.setNumberOfRequiredConfirmations(6);
        bitcoin.setReputation(8);


        // Ethereum
        Credentials ethCredentials;
        try {
            ethCredentials = WalletUtils.loadCredentials("YourPassword", "/path/to/your/cedentials/keyfile.json");
        } catch (IOException | CipherException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        EthereumService ethereumService = new EthereumService(NODE_URL_ETHEREUM_MAIN);  // metric collector goes to mainnet
        AbstractEthereumMetricCollector ethereumMetricCollector = new EthereumMetricCollector(
                ethereumService,
                new CryptocurrencyPriceService(),
                Currency.US_DOLLAR,
                25
        );
        AbstractDataAccessService ethereumDataAccessService = new EthereumDataAccessService(
                (str, maxL) -> Arrays.asList(str), (lst, maxL) -> lst, ethereumService
        );
        BlockchainMetaData ethereum = new BlockchainMetaData();
        ethereum.setIdentifier("Ethereum");
        ethereum.setMetricCollector(ethereumMetricCollector);
        ethereum.setDataAccessService(ethereumDataAccessService);
        ethereum.setNumberOfRequiredConfirmations(12);
        ethereum.setReputation(10);

        // Ethereum Classic
        Credentials etcCredentials;
        try {
            etcCredentials = WalletUtils.loadCredentials("YourPassword", "/path/to/your/cedentials/keyfile.json");
        } catch (IOException | CipherException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        EthereumService ethereumClassicService = new EthereumService(NODE_URL_ETHEREUM_CLASSIC_MAIN, etcCredentials);
        AbstractEthereumMetricCollector ethereumClassicMetricCollector = new EthereumClassicMetricCollector(
                ethereumClassicService,
                new CryptocurrencyPriceService(),
                Currency.US_DOLLAR,
                25
        );
        AbstractDataAccessService ethereumClassicDataAccessService = new EthereumDataAccessService(
                (str, maxL) -> Arrays.asList(str), (lst, maxL) -> lst, ethereumClassicService
        );
        BlockchainMetaData ethereumClassic = new BlockchainMetaData();
        ethereumClassic.setIdentifier("Ethereum Classic");
        ethereumClassic.setMetricCollector(ethereumClassicMetricCollector);
        ethereumClassic.setDataAccessService(ethereumClassicDataAccessService);
        ethereumClassic.setNumberOfRequiredConfirmations(12);
        ethereumClassic.setReputation(7);


        // Expanse
        Credentials expCredentials = null;
        try {
            expCredentials = WalletUtils.loadCredentials("YourPassword", "/path/to/your/cedentials/keyfile.json");
        } catch (IOException | CipherException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        ExpanseService expanseService = new ExpanseService(NODE_URL_EXPANSE_MAIN, expCredentials);
        AbstractEthereumMetricCollector expanseMetricCollector = new ExpanseMetricCollector(
                expanseService,
                new CryptocurrencyPriceService(),
                Currency.US_DOLLAR,
                10
        );
        AbstractDataAccessService expanseDataAccessService = new EthereumDataAccessService(
                (str, maxL) -> Arrays.asList(str), (lst, maxL) -> lst, expanseService
        );
        BlockchainMetaData expanse = new BlockchainMetaData();
        expanse.setIdentifier("Expanse");
        expanse.setMetricCollector(expanseMetricCollector);
        expanse.setDataAccessService(expanseDataAccessService);
        expanse.setNumberOfRequiredConfirmations(12);
        expanse.setReputation(4);

        blockchainMetaDataList.add(bitcoin);
        blockchainMetaDataList.add(ethereum);
        blockchainMetaDataList.add(ethereumClassic);
        blockchainMetaDataList.add(expanse);
    }

    private void createBlockchainManager() {
        WeightedRankingSettings rankingSettings = new WeightedRankingSettings();
        // switchover supression period
        rankingSettings.setTimeSpanBetweenTwoChains(1, ChronoUnit.MINUTES);
        rankingSettings.setCostsForWritingDataScoreFn(costsForWritingData -> {
            double mediumPriorityCosts = costsForWritingData.doubleValue();
            if (mediumPriorityCosts < 0.5) {
                return 4;
            }
            if (mediumPriorityCosts < 2.0) {
                return 3;
            }
            if (mediumPriorityCosts < 3.5) {
                return 2;
            }
            if (mediumPriorityCosts < 5) {
                return 1;
            }
            return 0;
        });
        rankingSettings.setCostsForWritingDataWeightFn(costsForWritingData -> 5);
        rankingSettings.setCostsForRetrievingDataScoreFn(costs -> 0);
        rankingSettings.setCostsForRetrievingDataWeightFn(costs -> 0);
        rankingSettings.setStorageFeeScoreFn(costs -> 0);
        rankingSettings.setStorageFeeWeightFn(costs -> 0);
        rankingSettings.setExchangeRateScoreFn(costs -> 0);
        rankingSettings.setExchangeRateWeightFn(costs -> 0);
        rankingSettings.setBlockTimeScoreFn(blockTime -> {
            if (blockTime < 15) {
                return 4;
            }
            if (blockTime < 30) {
                return 3;
            }
            if (blockTime < 45) {
                return 2;
            }
            if (blockTime < 60) {
                return 1;
            }
            return 0;
        });
        rankingSettings.setBlockTimeWeightFn(blockTime -> 4);
        rankingSettings.setTransactionThroughputScoreFn(txThroughput -> {
            if (txThroughput < 1) {
                return 0;
            }
            if (txThroughput < 2.5) {
                return 1;
            }
            if (txThroughput < 4) {
                return 2;
            }
            if (txThroughput < 5.5) {
                return 3;
            }
            return 4;
        });
        rankingSettings.setTransactionThroughputWeightFn(txThroughput -> 5);
        rankingSettings.setMiningDistributionScoreFn(costs -> 0);
        rankingSettings.setMiningDistributionWeightFn(costs -> 0);
        rankingSettings.setNetworkHashrateScoreFn(costs -> 0);
        rankingSettings.setNetworkHashrateWeightFn(costs -> 0);
        rankingSettings.setNumberOfConfirmationsScoreFn(costs -> 0);
        rankingSettings.setNumberOfConfirmationsWeightFn(costs -> 0);
        rankingSettings.setReputationScoreFn(costs -> 0);
        rankingSettings.setReputationWeightFn(costs -> 0);

        ThresholdValidationSettings validationSettings = new ThresholdValidationSettings();
        validationSettings.setCostsForWritingDataValidationSettings(
                new MetricValidationSettings<>(costsForWritingData -> costsForWritingData.doubleValue() > 1.5, 60, ChronoUnit.SECONDS));
        validationSettings.setBlockTimeValidationSettings(
                new MetricValidationSettings<>(blockTime -> blockTime > 30, 120, ChronoUnit.SECONDS));

        validationSettings.setSwitchoverDecisionFn((m1, m2, m3, m4, m5, m6, m7, m8, m9, m10) -> (!m1 && !m5));

        BlockchainMetaData currentBlockchain = blockchainMetaDataList.get(3); // expanse
        blockchainManager = BlockchainManager
                .newInstance()
                .setRankingSettings(rankingSettings)
                .setThresholdValidationSettings(validationSettings)
                .preSelectBlockchain(currentBlockchain)
                .addBlockchain(blockchainMetaDataList.get(0))  // bitcoin
                .addBlockchain(blockchainMetaDataList.get(1))  // ethereum
                .addBlockchain(blockchainMetaDataList.get(2))  // ethereum classic
                .addBlockchain(blockchainMetaDataList.get(3))  // expanse
                .build();
    }

    private void subscribeToSwitchoverSuggestions() {
        blockchainManager
                .getSwitchoverSuggestionObservable()
                .blockingSubscribe(suggestion -> {
                    // specify your logic for a new switchover suggestion (there is a more appropriate blockchain)
                    // e.g., perform a switchover by calling blockchainManager.switchOver();
                    LOG.info("New switchover suggestion received: " + suggestion.getNextBlockchainResult().getBlockchain().getIdentifier());
                }, error -> {
                    // handle errors (e.g., connection timeouts)
                });
    }

}
