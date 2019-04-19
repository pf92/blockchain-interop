package com.ieee19.bc.interop.pf.core;

import com.ieee19.bc.interop.pf.core.exception.DataReadingFailedException;
import com.ieee19.bc.interop.pf.core.exception.DataWritingFailedException;
import com.ieee19.bc.interop.pf.core.model.*;
import com.ieee19.bc.interop.pf.proxy.bitcoin.BitcoinDataAccessService;
import com.ieee19.bc.interop.pf.proxy.bitcoin.BitcoinMetricCollector;
import com.ieee19.bc.interop.pf.proxy.ethereum.EthereumDataAccessService;
import com.ieee19.bc.interop.pf.proxy.ethereum.EthereumMetricCollector;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BlockchainManagerTest {

    private static final int EXPECTED_ETHEREUM_WEIGHTED_SCORE = 138;
    private static final int EXPECTED_BITCOIN_WEIGHTED_SCORE = 110;
    private static final String EXPECTED_NEXT_BLOCKCHAIN = "Ethereum";
    private static final String EXPECTED_DATA = "Test Data";

    private BlockchainMetaData bitcoin;
    private BlockchainMetaData ethereum;
    private BlockchainManager blockchainManager;

    private void createBlockchains() {
        Map<String, Double> miningDistribution = new HashMap<>();
        miningDistribution.put("testAddr", 0.5);

        // Bitcoin
        BitcoinMetricCollector bitcoinMetricCollector = mock(BitcoinMetricCollector.class);
        when(bitcoinMetricCollector.getCostsForWritingDataObservable())
                .thenReturn(Observable.fromArray(BigDecimal.valueOf(2)));
        when(bitcoinMetricCollector.getCostsForRetrievingDataObservable())
                .thenReturn(Observable.fromArray(BigDecimal.valueOf(0)));
        when(bitcoinMetricCollector.getStorageFeeObservable())
                .thenReturn(Observable.fromArray(BigDecimal.valueOf(0)));
        when(bitcoinMetricCollector.getAvgBlockTimeObservable())
                .thenReturn(Observable.fromArray(2.0));
        when(bitcoinMetricCollector.getTransactionThroughputObservable())
                .thenReturn(Observable.fromArray(0.5));
        when(bitcoinMetricCollector.getExchangeRateObservable())
                .thenReturn(Observable.fromArray(BigDecimal.valueOf(2)));
        when(bitcoinMetricCollector.getNetworkHashrateObservable())
                .thenReturn(Observable.fromArray(2.0));
        when(bitcoinMetricCollector.getBlockPercentagePerMinerObservable())
                .thenReturn(Observable.fromArray(miningDistribution));
        AbstractDataAccessService bitcoinDataAccessService = mock(BitcoinDataAccessService.class);
        bitcoin = new BlockchainMetaData();
        bitcoin.setIdentifier("Bitcoin");
        bitcoin.setMetricCollector(bitcoinMetricCollector);
        bitcoin.setDataAccessService(bitcoinDataAccessService);
        bitcoin.setNumberOfRequiredConfirmations(6);
        bitcoin.setReputation(8);

        // Ethereum
        EthereumMetricCollector ethereumMetricCollector = mock(EthereumMetricCollector.class);
        when(ethereumMetricCollector.getCostsForWritingDataObservable())
                .thenReturn(Observable.fromArray(BigDecimal.valueOf(1)));
        when(ethereumMetricCollector.getCostsForRetrievingDataObservable())
                .thenReturn(Observable.fromArray(BigDecimal.valueOf(0)));
        when(ethereumMetricCollector.getStorageFeeObservable())
                .thenReturn(Observable.fromArray(BigDecimal.valueOf(0)));
        when(ethereumMetricCollector.getAvgBlockTimeObservable())
                .thenReturn(Observable.fromArray(1.0));
        when(ethereumMetricCollector.getTransactionThroughputObservable())
                .thenReturn(Observable.fromArray(2.0));
        when(ethereumMetricCollector.getExchangeRateObservable())
                .thenReturn(Observable.fromArray(BigDecimal.valueOf(1)));
        when(ethereumMetricCollector.getNetworkHashrateObservable())
                .thenReturn(Observable.fromArray(0.5));
        when(ethereumMetricCollector.getBlockPercentagePerMinerObservable())
                .thenReturn(Observable.fromArray(miningDistribution));
        AbstractDataAccessService ethereumDataAccessService = mock(EthereumDataAccessService.class);
        ethereum = new BlockchainMetaData();
        ethereum.setIdentifier("Ethereum");
        ethereum.setMetricCollector(ethereumMetricCollector);
        ethereum.setDataAccessService(ethereumDataAccessService);
        ethereum.setNumberOfRequiredConfirmations(12);
        ethereum.setReputation(8);
    }

    private void createBlockchainManager() {
        WeightedRankingSettings rankingSettings = new WeightedRankingSettings();
        rankingSettings.setCostsForWritingDataScoreFn(costsForWritingData -> costsForWritingData.doubleValue() > 1 ? 2 : 4);
        rankingSettings.setCostsForWritingDataWeightFn(costsForWritingData -> 5);
        rankingSettings.setCostsForRetrievingDataScoreFn(costs -> costs.doubleValue() > 1 ? 2 : 4);
        rankingSettings.setCostsForRetrievingDataWeightFn(costs -> 3);
        rankingSettings.setStorageFeeScoreFn(costs -> costs.doubleValue() > 1 ? 2 : 4);
        rankingSettings.setStorageFeeWeightFn(costs -> 5);
        rankingSettings.setExchangeRateScoreFn(exchangeRate -> exchangeRate.doubleValue() > 1 ? 2 : 4);
        rankingSettings.setExchangeRateWeightFn(exchangeRate -> 4);
        rankingSettings.setBlockTimeScoreFn(blockTime -> blockTime > 1 ? 2 : 4);
        rankingSettings.setBlockTimeWeightFn(blockTime -> 4);
        rankingSettings.setTransactionThroughputScoreFn(txThroughput -> txThroughput > 1 ? 4 : 2);
        rankingSettings.setTransactionThroughputWeightFn(txThroughput -> 5);
        rankingSettings.setMiningDistributionScoreFn(miner -> miner.values().iterator().next() > 1 ? 2 : 4);
        rankingSettings.setMiningDistributionWeightFn(miner -> 5);
        rankingSettings.setNetworkHashrateScoreFn(hashrate -> hashrate > 1 ? 4 : 2);
        rankingSettings.setNetworkHashrateWeightFn(hashrate -> 4);
        rankingSettings.setNumberOfConfirmationsScoreFn(confirmations -> confirmations > 1 ? 2 : 4);
        rankingSettings.setNumberOfConfirmationsWeightFn(confirmations -> 3);
        rankingSettings.setReputationScoreFn(reputation -> 5);
        rankingSettings.setReputationWeightFn(reputation -> 0);

        blockchainManager = BlockchainManager
                .newInstance()
                .setRankingSettings(rankingSettings)
                .setThresholdValidationSettings(new ThresholdValidationSettings())
                .preSelectBlockchain(ethereum)
                .addBlockchain(ethereum)
                .addBlockchain(bitcoin)
                .build();
    }

    @BeforeEach
    public void setUp() {
        createBlockchains();
        createBlockchainManager();
    }

    @Test
    public void testWeightedScoreCalculationBitcoin_shouldReturnCorrectResults() {
        TestObserver<BlockchainCalculationResult> testObserver = new TestObserver<>();
        blockchainManager
                .getBlockchainCalculationResultObservables()
                .get(bitcoin.getIdentifier())
                .subscribe(testObserver);
        testObserver
                .assertNoErrors()
                .assertValue(result -> result.getOverallWeightedScore() == EXPECTED_BITCOIN_WEIGHTED_SCORE);
    }

    @Test
    public void testWeightedScoreCalculationEthereum_shouldReturnCorrectResults() {
        TestObserver<BlockchainCalculationResult> testObserver = new TestObserver<>();
        blockchainManager
                .getBlockchainCalculationResultObservables()
                .get(ethereum.getIdentifier())
                .subscribe(testObserver);
        testObserver
                .assertNoErrors()
                .assertValue(result -> result.getOverallWeightedScore() == EXPECTED_ETHEREUM_WEIGHTED_SCORE);
    }

    @Test
    public void testSwitchOverSuggestion_shouldReturnEthereum() {
        TestObserver<SwitchoverSuggestion> testObserver = new TestObserver<>();
        blockchainManager
                .getSwitchoverSuggestionObservable()
                .subscribe(testObserver);
        testObserver
                .assertNoErrors()
                .assertValue(switchoverSuggestion ->
                        switchoverSuggestion.getNextBlockchainResult().getBlockchain().getIdentifier().equals(EXPECTED_NEXT_BLOCKCHAIN));
    }

    @Test
    public void testWriteData_shouldCallCorrectMethod() throws DataWritingFailedException {
        doNothing().when(ethereum.getDataAccessService()).writeData(anyString());
        blockchainManager.writeData(EXPECTED_DATA);
        verify(ethereum.getDataAccessService()).writeData(EXPECTED_DATA);
    }

    @Test
    public void testSwitchOver_shouldCallCorrectMethods() throws DataReadingFailedException, DataWritingFailedException {
        List<String> expectedData = Arrays.asList("Test1", "Test2", "Test3");
        when(ethereum.getDataAccessService().getData(any(ZonedDateTime.class), any(ZonedDateTime.class)))
                .thenReturn(expectedData);
        doNothing().when(bitcoin.getDataAccessService()).writeData(anyString());
        ZonedDateTime from = ZonedDateTime.now().minus(1, DAYS);
        ZonedDateTime to = ZonedDateTime.now();

        blockchainManager.switchOver(bitcoin, from, to);

        verify(ethereum.getDataAccessService()).getData(from, to);
        verify(bitcoin.getDataAccessService()).writeData(expectedData);
    }

}
