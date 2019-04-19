package com.ieee19.bc.interop.pf.core.model;

import com.ieee19.bc.interop.pf.core.SwitchoverDecisionFunction;

import java.math.BigDecimal;
import java.util.Map;

/**
 * This class represents settings for a switchover descision and for each supported blockchain metric.
 */
public class ThresholdValidationSettings {

    private SwitchoverDecisionFunction switchoverDecisionFn;

    private MetricValidationSettings<BigDecimal> costsForWritingDataValidationSettings;

    private MetricValidationSettings<BigDecimal> costsForRetrievingDataValidationSettings;

    private MetricValidationSettings<BigDecimal> storageFeeValidationSettings;

    private MetricValidationSettings<BigDecimal> exchangeRateValidationSettings;

    private MetricValidationSettings<Double> blockTimeValidationSettings;

    private MetricValidationSettings<Double> transactionThroughputValidationSettings;

    private MetricValidationSettings<Map<String, Double>> miningDistributionValidationSettings;

    private MetricValidationSettings<Double> networkHashrateValidationSettings;

    private MetricValidationSettings<Integer> numberOfConfirmationsValidationSettings;

    private MetricValidationSettings<Integer> reputationValidationSettings;

    public SwitchoverDecisionFunction getSwitchoverDecisionFn() {
        return switchoverDecisionFn;
    }

    public void setSwitchoverDecisionFn(SwitchoverDecisionFunction switchoverDecisionFn) {
        this.switchoverDecisionFn = switchoverDecisionFn;
    }

    public MetricValidationSettings<BigDecimal> getCostsForWritingDataValidationSettings() {
        return costsForWritingDataValidationSettings;
    }

    public void setCostsForWritingDataValidationSettings(MetricValidationSettings<BigDecimal> costsForWritingDataValidationSettings) {
        this.costsForWritingDataValidationSettings = costsForWritingDataValidationSettings;
    }

    public MetricValidationSettings<BigDecimal> getCostsForRetrievingDataValidationSettings() {
        return costsForRetrievingDataValidationSettings;
    }

    public void setCostsForRetrievingDataValidationSettings(MetricValidationSettings<BigDecimal> costsForRetrievingDataValidationSettings) {
        this.costsForRetrievingDataValidationSettings = costsForRetrievingDataValidationSettings;
    }

    public MetricValidationSettings<BigDecimal> getStorageFeeValidationSettings() {
        return storageFeeValidationSettings;
    }

    public void setStorageFeeValidationSettings(MetricValidationSettings<BigDecimal> storageFeeValidationSettings) {
        this.storageFeeValidationSettings = storageFeeValidationSettings;
    }

    public MetricValidationSettings<BigDecimal> getExchangeRateValidationSettings() {
        return exchangeRateValidationSettings;
    }

    public void setExchangeRateValidationSettings(MetricValidationSettings<BigDecimal> exchangeRateValidationSettings) {
        this.exchangeRateValidationSettings = exchangeRateValidationSettings;
    }

    public MetricValidationSettings<Double> getBlockTimeValidationSettings() {
        return blockTimeValidationSettings;
    }

    public void setBlockTimeValidationSettings(MetricValidationSettings<Double> blockTimeValidationSettings) {
        this.blockTimeValidationSettings = blockTimeValidationSettings;
    }

    public MetricValidationSettings<Double> getTransactionThroughputValidationSettings() {
        return transactionThroughputValidationSettings;
    }

    public void setTransactionThroughputValidationSettings(MetricValidationSettings<Double> transactionThroughputValidationSettings) {
        this.transactionThroughputValidationSettings = transactionThroughputValidationSettings;
    }

    public MetricValidationSettings<Map<String, Double>> getMiningDistributionValidationSettings() {
        return miningDistributionValidationSettings;
    }

    public void setMiningDistributionValidationSettings(MetricValidationSettings<Map<String, Double>> miningDistributionValidationSettings) {
        this.miningDistributionValidationSettings = miningDistributionValidationSettings;
    }

    public MetricValidationSettings<Double> getNetworkHashrateValidationSettings() {
        return networkHashrateValidationSettings;
    }

    public void setNetworkHashrateValidationSettings(MetricValidationSettings<Double> networkHashrateValidationSettings) {
        this.networkHashrateValidationSettings = networkHashrateValidationSettings;
    }

    public MetricValidationSettings<Integer> getNumberOfConfirmationsValidationSettings() {
        return numberOfConfirmationsValidationSettings;
    }

    public void setNumberOfConfirmationsValidationSettings(MetricValidationSettings<Integer> numberOfConfirmationsValidationSettings) {
        this.numberOfConfirmationsValidationSettings = numberOfConfirmationsValidationSettings;
    }

    public MetricValidationSettings<Integer> getReputationValidationSettings() {
        return reputationValidationSettings;
    }

    public void setReputationValidationSettings(MetricValidationSettings<Integer> reputationValidationSettings) {
        this.reputationValidationSettings = reputationValidationSettings;
    }

}
