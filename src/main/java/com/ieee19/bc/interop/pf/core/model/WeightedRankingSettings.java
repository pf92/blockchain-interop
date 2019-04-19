package com.ieee19.bc.interop.pf.core.model;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.function.Function;

/**
 * This class represents the settings for the weighted ranking containing the assignement of weights to each metric
 * and score functions for deciding which score should be assigned to a metric for a certain value.
 */
public class WeightedRankingSettings {

    private long timeSpanBetweenTwoChains = 0;  // time span between to different switchover suggestion (prevent switching back and forth between chains)

    private ChronoUnit timeUnit = ChronoUnit.SECONDS;

    private Function<BigDecimal, Integer> costsForWritingDataWeightFn;

    private Function<BigDecimal, Integer> costsForRetrievingDataWeightFn;

    private Function<BigDecimal, Integer> storageFeeWeightFn;

    private Function<BigDecimal, Integer> exchangeRateWeightFn;

    private Function<Double, Integer> blockTimeWeightFn;

    private Function<Double, Integer> transactionThroughputWeightFn;

    private Function<Map<String, Double>, Integer> miningDistributionWeightFn;

    private Function<Double, Integer> networkHashrateWeightFn;

    private Function<Integer, Integer> numberOfConfirmationsWeightFn;

    private Function<Integer, Integer> reputationWeightFn;

    private Function<BigDecimal, Integer> costsForWritingDataScoreFn;

    private Function<BigDecimal, Integer> costsForRetrievingDataScoreFn;

    private Function<BigDecimal, Integer> storageFeeScoreFn;

    private Function<BigDecimal, Integer> exchangeRateScoreFn;

    private Function<Double, Integer> blockTimeScoreFn;

    private Function<Double, Integer> transactionThroughputScoreFn;

    private Function<Map<String, Double>, Integer> miningDistributionScoreFn;

    private Function<Double, Integer> networkHashrateScoreFn;

    private Function<Integer, Integer> numberOfConfirmationsScoreFn;

    private Function<Integer, Integer> reputationScoreFn;

    public long getTimeSpanBetweenTwoChains() {
        return timeSpanBetweenTwoChains;
    }

    public void setTimeSpanBetweenTwoChains(long timeSpanBetweenTwoChains, ChronoUnit timeUnit) {
        this.timeSpanBetweenTwoChains = timeSpanBetweenTwoChains;
        this.timeUnit = timeUnit;
    }

    public ChronoUnit getTimeUnit() {
        return timeUnit;
    }

    public Function<BigDecimal, Integer> getCostsForWritingDataWeightFn() {
        return costsForWritingDataWeightFn;
    }

    public void setCostsForWritingDataWeightFn(Function<BigDecimal, Integer> costsForWritingDataWeightFn) {
        this.costsForWritingDataWeightFn = costsForWritingDataWeightFn;
    }

    public Function<BigDecimal, Integer> getCostsForRetrievingDataWeightFn() {
        return costsForRetrievingDataWeightFn;
    }

    public void setCostsForRetrievingDataWeightFn(Function<BigDecimal, Integer> costsForRetrievingDataWeightFn) {
        this.costsForRetrievingDataWeightFn = costsForRetrievingDataWeightFn;
    }

    public Function<BigDecimal, Integer> getStorageFeeWeightFn() {
        return storageFeeWeightFn;
    }

    public void setStorageFeeWeightFn(Function<BigDecimal, Integer> storageFeeWeightFn) {
        this.storageFeeWeightFn = storageFeeWeightFn;
    }

    public Function<BigDecimal, Integer> getExchangeRateWeightFn() {
        return exchangeRateWeightFn;
    }

    public void setExchangeRateWeightFn(Function<BigDecimal, Integer> exchangeRateWeightFn) {
        this.exchangeRateWeightFn = exchangeRateWeightFn;
    }

    public Function<Double, Integer> getBlockTimeWeightFn() {
        return blockTimeWeightFn;
    }

    public void setBlockTimeWeightFn(Function<Double, Integer> blockTimeWeightFn) {
        this.blockTimeWeightFn = blockTimeWeightFn;
    }

    public Function<Double, Integer> getTransactionThroughputWeightFn() {
        return transactionThroughputWeightFn;
    }

    public void setTransactionThroughputWeightFn(Function<Double, Integer> transactionThroughputWeightFn) {
        this.transactionThroughputWeightFn = transactionThroughputWeightFn;
    }

    public Function<Map<String, Double>, Integer> getMiningDistributionWeightFn() {
        return miningDistributionWeightFn;
    }

    public void setMiningDistributionWeightFn(Function<Map<String, Double>, Integer> miningDistributionWeightFn) {
        this.miningDistributionWeightFn = miningDistributionWeightFn;
    }

    public Function<Double, Integer> getNetworkHashrateWeightFn() {
        return networkHashrateWeightFn;
    }

    public void setNetworkHashrateWeightFn(Function<Double, Integer> networkHashrateWeightFn) {
        this.networkHashrateWeightFn = networkHashrateWeightFn;
    }

    public Function<Integer, Integer> getNumberOfConfirmationsWeightFn() {
        return numberOfConfirmationsWeightFn;
    }

    public void setNumberOfConfirmationsWeightFn(Function<Integer, Integer> numberOfConfirmationsWeightFn) {
        this.numberOfConfirmationsWeightFn = numberOfConfirmationsWeightFn;
    }

    public Function<Integer, Integer> getReputationWeightFn() {
        return reputationWeightFn;
    }

    public void setReputationWeightFn(Function<Integer, Integer> reputationWeightFn) {
        this.reputationWeightFn = reputationWeightFn;
    }

    public Function<BigDecimal, Integer> getCostsForWritingDataScoreFn() {
        return costsForWritingDataScoreFn;
    }

    public void setCostsForWritingDataScoreFn(Function<BigDecimal, Integer> costsForWritingDataScoreFn) {
        this.costsForWritingDataScoreFn = costsForWritingDataScoreFn;
    }

    public Function<BigDecimal, Integer> getCostsForRetrievingDataScoreFn() {
        return costsForRetrievingDataScoreFn;
    }

    public void setCostsForRetrievingDataScoreFn(Function<BigDecimal, Integer> costsForRetrievingDataScoreFn) {
        this.costsForRetrievingDataScoreFn = costsForRetrievingDataScoreFn;
    }

    public Function<BigDecimal, Integer> getStorageFeeScoreFn() {
        return storageFeeScoreFn;
    }

    public void setStorageFeeScoreFn(Function<BigDecimal, Integer> storageFeeScoreFn) {
        this.storageFeeScoreFn = storageFeeScoreFn;
    }

    public Function<BigDecimal, Integer> getExchangeRateScoreFn() {
        return exchangeRateScoreFn;
    }

    public void setExchangeRateScoreFn(Function<BigDecimal, Integer> exchangeRateScoreFn) {
        this.exchangeRateScoreFn = exchangeRateScoreFn;
    }

    public Function<Double, Integer> getBlockTimeScoreFn() {
        return blockTimeScoreFn;
    }

    public void setBlockTimeScoreFn(Function<Double, Integer> blockTimeScoreFn) {
        this.blockTimeScoreFn = blockTimeScoreFn;
    }

    public Function<Double, Integer> getTransactionThroughputScoreFn() {
        return transactionThroughputScoreFn;
    }

    public void setTransactionThroughputScoreFn(Function<Double, Integer> transactionThroughputScoreFn) {
        this.transactionThroughputScoreFn = transactionThroughputScoreFn;
    }

    public Function<Map<String, Double>, Integer> getMiningDistributionScoreFn() {
        return miningDistributionScoreFn;
    }

    public void setMiningDistributionScoreFn(Function<Map<String, Double>, Integer> miningDistributionScoreFn) {
        this.miningDistributionScoreFn = miningDistributionScoreFn;
    }

    public Function<Double, Integer> getNetworkHashrateScoreFn() {
        return networkHashrateScoreFn;
    }

    public void setNetworkHashrateScoreFn(Function<Double, Integer> networkHashrateScoreFn) {
        this.networkHashrateScoreFn = networkHashrateScoreFn;
    }

    public Function<Integer, Integer> getNumberOfConfirmationsScoreFn() {
        return numberOfConfirmationsScoreFn;
    }

    public void setNumberOfConfirmationsScoreFn(Function<Integer, Integer> numberOfConfirmationsScoreFn) {
        this.numberOfConfirmationsScoreFn = numberOfConfirmationsScoreFn;
    }

    public Function<Integer, Integer> getReputationScoreFn() {
        return reputationScoreFn;
    }

    public void setReputationScoreFn(Function<Integer, Integer> reputationScoreFn) {
        this.reputationScoreFn = reputationScoreFn;
    }

}
