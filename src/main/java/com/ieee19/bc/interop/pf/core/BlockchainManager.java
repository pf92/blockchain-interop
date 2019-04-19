package com.ieee19.bc.interop.pf.core;

import com.ieee19.bc.interop.pf.core.exception.DataReadingFailedException;
import com.ieee19.bc.interop.pf.core.exception.DataWritingFailedException;
import com.ieee19.bc.interop.pf.core.model.*;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.ieee19.bc.interop.pf.core.model.BlockchainMetric.*;

/**
 * This class represents the core manager that is responsible for determining the most beneficial blockchain.
 *
 */
public class BlockchainManager {

    private static final Logger LOG = LoggerFactory.getLogger(BlockchainManager.class);
    private Set<BlockchainMetaData> blockchains = new HashSet<>();
    private Observable<SwitchoverSuggestion> switchoverSuggestionObservable;
    private WeightedRankingSettings rankingSettings;
    private ThresholdValidationSettings thresholdValidationSettings;
    private BlockchainMetaData currentBlockchain;
    private List<String> dataBuffer = new ArrayList<>();
    private boolean isSwitchover = false;
    private Object switchOverLock = new Object();
    private Map<String, Observable<BlockchainCalculationResult>> bcCalculationResultObservables = new HashMap<>();

    private BlockchainManager() {
    }

    public static BlockchainManager newInstance() {
        return new BlockchainManager();
    }

    /**
     * Calculates the weighted score for the given blockchain metric values.
     *
     * @param writingCosts
     * @param retrievingCosts
     * @param storageCosts
     * @param priceInFiat
     * @param blockTime
     * @param txThroughput
     * @param miningDistribution
     * @param networkHashrate
     * @param numberOfRequiredConfirmations
     * @param reputation
     * @return the calculated weighted scores
     */
    private BlockchainWeightedScore calculateBlockchainWeightedScore(BigDecimal writingCosts,
                                                                     BigDecimal retrievingCosts, BigDecimal storageCosts,
                                                                     BigDecimal priceInFiat, Double blockTime,
                                                                     Double txThroughput, Map<String, Double> miningDistribution,
                                                                     Double networkHashrate, int numberOfRequiredConfirmations,
                                                                     int reputation) {
        BlockchainWeightedScore score = new BlockchainWeightedScore();
        score.setWeightedMetricScore(COSTS_FOR_WRITING_DATA,
                rankingSettings.getCostsForWritingDataScoreFn().apply(writingCosts) *
                        rankingSettings.getCostsForWritingDataWeightFn().apply(writingCosts));
        score.setWeightedMetricScore(COSTS_FOR_RETRIEVING_DATA,
                rankingSettings.getCostsForRetrievingDataScoreFn().apply(retrievingCosts) *
                        rankingSettings.getCostsForRetrievingDataWeightFn().apply(retrievingCosts));
        score.setWeightedMetricScore(STORAGE_FEES,
                rankingSettings.getStorageFeeScoreFn().apply(storageCosts) *
                        rankingSettings.getStorageFeeWeightFn().apply(storageCosts));
        score.setWeightedMetricScore(EXCHANGE_RATE,
                rankingSettings.getExchangeRateScoreFn().apply(priceInFiat) *
                        rankingSettings.getExchangeRateWeightFn().apply(priceInFiat));
        score.setWeightedMetricScore(BLOCK_TIME,
                rankingSettings.getBlockTimeScoreFn().apply(blockTime) *
                        rankingSettings.getBlockTimeWeightFn().apply(blockTime));
        score.setWeightedMetricScore(TRANSACTION_THROUGHPUT,
                rankingSettings.getTransactionThroughputScoreFn().apply(txThroughput) *
                        rankingSettings.getTransactionThroughputWeightFn().apply(txThroughput));
        score.setWeightedMetricScore(MINING_DISTRIBUTION,
                rankingSettings.getMiningDistributionScoreFn().apply(miningDistribution) *
                        rankingSettings.getMiningDistributionWeightFn().apply(miningDistribution));
        score.setWeightedMetricScore(NETWORK_HASHRATE,
                rankingSettings.getNetworkHashrateScoreFn().apply(networkHashrate) *
                        rankingSettings.getNetworkHashrateWeightFn().apply(networkHashrate));
        score.setWeightedMetricScore(NUMBER_OF_REQUIRED_CONFIRMATIONS,
                rankingSettings.getNumberOfConfirmationsScoreFn().apply(numberOfRequiredConfirmations) *
                        rankingSettings.getNumberOfConfirmationsWeightFn().apply(numberOfRequiredConfirmations));
        score.setWeightedMetricScore(REPUTATION,
                rankingSettings.getReputationScoreFn().apply(reputation) *
                        rankingSettings.getReputationWeightFn().apply(reputation));

        return score;
    }

    /**
     * Creates an {@link Observable} for observing validation results of a particular blockchain metric.
     *
     * @param metricObservable   the observable that represents the stream of metric values
     * @param validationSettings the validation settings
     * @return an {@link Observable} for observing validation results (true or false values)
     */
    private <T> Observable<Boolean> createMetricValidationObservable(Observable<T> metricObservable,
                                                                     MetricValidationSettings<T> validationSettings) {
        Function<T, Boolean> validationFn = metricValue -> true;
        long timeSpan = 0;
        ChronoUnit timeUnit = ChronoUnit.SECONDS;

        if (validationSettings != null) {
            validationFn = validationSettings.getThresholdValidationFn();
            timeSpan = validationSettings.getViolationTimeSpan();
            timeUnit = validationSettings.getViolationTimeUnit();
        }

        return metricObservable
                .map(validationFn::apply)
                .compose(new ThresholdViolationOperator(timeSpan, timeUnit));
    }

    /**
     * Combines the latest threshold validation values of each blockchain metric and creates a threshold validation
     * result for a given blockchain
     *
     * @param blockchain the blockchain a threshold validation result should be created for
     * @return a threshold validation result for the given blockchain
     */
    private Observable<ThresholdValidationResult> createBlockchainThresholdValidationObservable(BlockchainMetaData blockchain) {
        List<Observable<Boolean>> metricValidationResults = new ArrayList<>();
        IMetricCollector metricCollector = blockchain.getMetricCollector();

        metricValidationResults.add(
                createMetricValidationObservable(
                        metricCollector.getCostsForWritingDataObservable(),
                        thresholdValidationSettings.getCostsForWritingDataValidationSettings()
                ));
        metricValidationResults.add(
                createMetricValidationObservable(
                        metricCollector.getCostsForRetrievingDataObservable(),
                        thresholdValidationSettings.getCostsForRetrievingDataValidationSettings()
                ));
        metricValidationResults.add(
                createMetricValidationObservable(
                        metricCollector.getStorageFeeObservable(),
                        thresholdValidationSettings.getStorageFeeValidationSettings()
                ));
        metricValidationResults.add(
                createMetricValidationObservable(
                        metricCollector.getExchangeRateObservable(),
                        thresholdValidationSettings.getExchangeRateValidationSettings()
                ));
        metricValidationResults.add(
                createMetricValidationObservable(
                        metricCollector.getAvgBlockTimeObservable(),
                        thresholdValidationSettings.getBlockTimeValidationSettings()
                ));
        metricValidationResults.add(
                createMetricValidationObservable(
                        metricCollector.getTransactionThroughputObservable(),
                        thresholdValidationSettings.getTransactionThroughputValidationSettings()
                ));
        metricValidationResults.add(
                createMetricValidationObservable(
                        metricCollector.getBlockPercentagePerMinerObservable(),
                        thresholdValidationSettings.getMiningDistributionValidationSettings()
                ));
        metricValidationResults.add(
                createMetricValidationObservable(
                        metricCollector.getNetworkHashrateObservable(),
                        thresholdValidationSettings.getNetworkHashrateValidationSettings()
                ));

        return Observable.combineLatest(metricValidationResults, results -> {
                    ThresholdValidationResult validationResult = new ThresholdValidationResult();

                    validationResult.setThresholdValidationResult(COSTS_FOR_WRITING_DATA, (Boolean) results[0]);
                    validationResult.setThresholdValidationResult(COSTS_FOR_RETRIEVING_DATA, (Boolean) results[1]);
                    validationResult.setThresholdValidationResult(STORAGE_FEES, (Boolean) results[2]);
                    validationResult.setThresholdValidationResult(EXCHANGE_RATE, (Boolean) results[3]);
                    validationResult.setThresholdValidationResult(BLOCK_TIME, (Boolean) results[4]);
                    validationResult.setThresholdValidationResult(TRANSACTION_THROUGHPUT, (Boolean) results[5]);
                    validationResult.setThresholdValidationResult(MINING_DISTRIBUTION, (Boolean) results[6]);
                    validationResult.setThresholdValidationResult(NETWORK_HASHRATE, (Boolean) results[7]);

                    boolean result = true;
                    if (thresholdValidationSettings.getNumberOfConfirmationsValidationSettings() != null) {
                        result = thresholdValidationSettings
                                .getNumberOfConfirmationsValidationSettings()
                                .getThresholdValidationFn()
                                .apply(blockchain.getNumberOfRequiredConfirmations());
                    }
                    validationResult.setThresholdValidationResult(NUMBER_OF_REQUIRED_CONFIRMATIONS, result);

                    result = true;
                    if (thresholdValidationSettings.getReputationValidationSettings() != null) {
                        result = thresholdValidationSettings
                                .getReputationValidationSettings()
                                .getThresholdValidationFn()
                                .apply(blockchain.getReputation());
                    }
                    validationResult.setThresholdValidationResult(REPUTATION, result);

                    return validationResult;
                }
        );
    }

    /**
     * Adds a blockchain.
     *
     * @param blockchain the blockchain to add.
     */
    public BlockchainManager addBlockchain(BlockchainMetaData blockchain) {
        blockchains.add(blockchain);
        return this;
    }

    /**
     * Adds blockchains.
     *
     * @param blockchains the blockchains to add.
     */
    public BlockchainManager addBlockchains(Collection<BlockchainMetaData> blockchains) {
        blockchains.addAll(blockchains);
        return this;
    }

    /**
     * Designates a blockchain as the <i>current blockchain</i> that is used for read and write operations.
     *
     * @param blockchain the new current blockchain
     */
    public BlockchainManager preSelectBlockchain(BlockchainMetaData blockchain) {
        currentBlockchain = blockchain;
        return this;
    }

    /**
     * Initializes the {@link BlockchainManager} and creates the switchover {@link Observable} for observing
     * switchover suggestions.
     */
    public BlockchainManager build() {
        List<Observable<BlockchainCalculationResult>> blockchainCalculationResults = new ArrayList<>();

        for (BlockchainMetaData blockchain : blockchains) {
            IMetricCollector metricCollector = blockchain.getMetricCollector();
            Observable<BlockchainWeightedScore> blockchainWeightedScoreObservable = Observable
                    .combineLatest(
                            metricCollector.getCostsForWritingDataObservable().distinctUntilChanged(),
                            metricCollector.getCostsForRetrievingDataObservable().distinctUntilChanged(),
                            metricCollector.getStorageFeeObservable().distinctUntilChanged(),
                            metricCollector.getExchangeRateObservable().distinctUntilChanged(),
                            metricCollector.getAvgBlockTimeObservable().distinctUntilChanged(),
                            metricCollector.getTransactionThroughputObservable().distinctUntilChanged(),
                            metricCollector.getBlockPercentagePerMinerObservable().distinctUntilChanged(),
                            metricCollector.getNetworkHashrateObservable().distinctUntilChanged(),
                            (writingCosts, retrievingCosts, storageCosts, priceInFiat, blockTime, txThroughput, miningDistribution, networkHashrate) -> {
                                BlockchainWeightedScore score = calculateBlockchainWeightedScore(writingCosts, retrievingCosts,
                                        storageCosts, priceInFiat, blockTime, txThroughput, miningDistribution, networkHashrate,
                                        blockchain.getNumberOfRequiredConfirmations(), blockchain.getReputation());
                                score.setBlockchain(blockchain);
                                return score;
                            }
                    );
            Observable<ThresholdValidationResult> thresholdValidationObservable =
                    createBlockchainThresholdValidationObservable(blockchain);

            Observable<BlockchainCalculationResult> blockchainCalculationResult =
                    Observable.combineLatest(blockchainWeightedScoreObservable, thresholdValidationObservable, (score, validationResult) -> {
                        BlockchainCalculationResult calculationResult = new BlockchainCalculationResult();

                        calculationResult.setBlockchain(blockchain);
                        calculationResult.setWeightedScores(score.getWeightedMetricScores());
                        calculationResult.setThresholdValidationResults(validationResult.getThresholdValidationResults());

                        return calculationResult;
                    });

            blockchainCalculationResults.add(blockchainCalculationResult);
            bcCalculationResultObservables.put(blockchain.getIdentifier(), blockchainCalculationResult);
        }
        switchoverSuggestionObservable = Observable
                .combineLatest(blockchainCalculationResults, (calculationResults) -> {
                    BlockchainCalculationResult mostBeneficialResult = null;
                    BlockchainCalculationResult currentChainResult = null;

                    for (int i = 0; i < calculationResults.length; i++) {
                        BlockchainCalculationResult calculationResult = (BlockchainCalculationResult) calculationResults[i];
                        if (calculationResult.getBlockchain().equals(currentBlockchain)) {
                            currentChainResult = calculationResult;
                        }
                        boolean isMoreBeneficial = mostBeneficialResult == null ||
                                mostBeneficialResult.getOverallWeightedScore() <= calculationResult.getOverallWeightedScore();
                        boolean switchoverDecision = callSwitchoverDecisionFn(calculationResult.getThresholdValidationResults());
                        if (isMoreBeneficial && !switchoverDecision) {
                            // there is a more beneficial blockchain that does not violated any threshold
                            mostBeneficialResult = calculationResult;
                        }
                    }

                    Optional<SwitchoverSuggestion> switchoverSuggestionOptional = Optional.empty();
                    if (mostBeneficialResult != null) {
                        SwitchoverSuggestion switchoverSuggestion = new SwitchoverSuggestion();
                        switchoverSuggestion.setCurrentBlockchainResult(currentChainResult);
                        switchoverSuggestion.setNextBlockchainResult(mostBeneficialResult);
                        switchoverSuggestionOptional = Optional.of(switchoverSuggestion);
                    }

                    return switchoverSuggestionOptional;
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinctUntilChanged(switchoverSuggestion ->
                        switchoverSuggestion
                                .getNextBlockchainResult()
                                .getBlockchain()
                                .getIdentifier()
                )
                .throttleLatest(rankingSettings.getTimeSpanBetweenTwoChains(), TimeUnit.of(rankingSettings.getTimeUnit()))
                .share()    // multicast
                .replay(1)
                .autoConnect();

        return this;
    }

    /**
     * Applies the metric validation results of a blockchain to a function (provided via the settings) that decides whether a
     * switchover should be suggested (true) or not (false)
     *
     * @param metricValidationResults the metric validations results of a blockchain
     * @return true if a switchover is suggested, otherwise false
     */
    private boolean callSwitchoverDecisionFn(Map<BlockchainMetric, Boolean> metricValidationResults) {
        if (thresholdValidationSettings.getSwitchoverDecisionFn() == null) {
            return false;
        }
        return thresholdValidationSettings
                .getSwitchoverDecisionFn()
                .apply(
                        metricValidationResults.get(COSTS_FOR_WRITING_DATA),
                        metricValidationResults.get(COSTS_FOR_RETRIEVING_DATA),
                        metricValidationResults.get(STORAGE_FEES),
                        metricValidationResults.get(EXCHANGE_RATE),
                        metricValidationResults.get(BLOCK_TIME),
                        metricValidationResults.get(TRANSACTION_THROUGHPUT),
                        metricValidationResults.get(MINING_DISTRIBUTION),
                        metricValidationResults.get(NETWORK_HASHRATE),
                        metricValidationResults.get(NUMBER_OF_REQUIRED_CONFIRMATIONS),
                        metricValidationResults.get(REPUTATION)
                );
    }

    public Map<String, Observable<BlockchainCalculationResult>> getBlockchainCalculationResultObservables() {
        return bcCalculationResultObservables;
    }

    public Observable<SwitchoverSuggestion> getSwitchoverSuggestionObservable() {
        return switchoverSuggestionObservable;
    }

    public BlockchainManager setRankingSettings(WeightedRankingSettings rankingSettings) {
        this.rankingSettings = rankingSettings;
        return this;
    }

    public BlockchainManager setThresholdValidationSettings(ThresholdValidationSettings thresholdValidationSettings) {
        this.thresholdValidationSettings = thresholdValidationSettings;
        return this;
    }

    /**
     * Performs a switchover to <i>nextBlockchain</i>. The amoung of data that is moved to the specified blockchain
     * depends on the <i>from</i> and <i>to</i> dates.
     *
     * @param nextBlockchain
     * @param from
     * @param to
     * @throws DataReadingFailedException
     * @throws DataWritingFailedException
     */
    public synchronized void switchOver(BlockchainMetaData nextBlockchain, ZonedDateTime from, ZonedDateTime to)
            throws DataReadingFailedException, DataWritingFailedException {
        LOG.info("start switchover");
        synchronized (switchOverLock) {
            isSwitchover = true;
        }
        List<String> dataToTransfer = currentBlockchain.getDataAccessService().getData(from, to);
        nextBlockchain.getDataAccessService().writeData(dataToTransfer);
        synchronized (switchOverLock) {
            isSwitchover = false;
            currentBlockchain = nextBlockchain;
        }

        LOG.info("switchover finished");

        synchronized (switchOverLock) {
            // flush data buffer to new blockchain
            LOG.info("flush buffer");
            for (String str : dataBuffer) {
                nextBlockchain.getDataAccessService().writeData(str);
            }
            dataBuffer.clear();
        }
    }

    /**
     * Writes a string to the current blockchain.
     *
     * @param data the string to write
     * @throws DataWritingFailedException
     */
    public void writeData(String data) throws DataWritingFailedException {
        synchronized (switchOverLock) {
            LOG.info("Entered write data method");
            if (isSwitchover) {
                LOG.info("write " + data + " to buffer");
                dataBuffer.add(data);
            } else {
                currentBlockchain.getDataAccessService().writeData(data);
            }
            LOG.info("Exit write data method");
        }
    }

}
