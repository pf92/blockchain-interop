package com.ieee19.bc.interop.pf.core.model;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the threshold validation result for each metric of a blockchain.
 */
public class ThresholdValidationResult {

    /**
     * Contains a boolean value for each metric indicating whether a metric has passed threshold(s) (true)
     * or has violated all or some of them (false).
     */
    private Map<BlockchainMetric, Boolean> thresholdValidationResults = new HashMap<>();

    public void setThresholdValidationResult(BlockchainMetric metric, Boolean result) {
        thresholdValidationResults.put(metric, result);
    }

    public Boolean getThresholdValidationResult(BlockchainMetric metric) {
        return thresholdValidationResults.get(metric);
    }

    public Map<BlockchainMetric, Boolean> getThresholdValidationResults() {
        return thresholdValidationResults;
    }

    public void setThresholdValidationResults(Map<BlockchainMetric, Boolean> thresholdValidationResults) {
        this.thresholdValidationResults = thresholdValidationResults;
    }

}
