package com.ieee19.bc.interop.pf.core.model;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the weighted scores and the threshold validation results of a blockchain com some time.
 */
public class BlockchainCalculationResult {

    private BlockchainMetaData blockchain;

    /**
     * Contains the weighted score for each blockchain metric.
     */
    private Map<BlockchainMetric, Integer> weightedScores = new HashMap<>();

    /**
     * Contains a boolean value for each metric indicating whether a metric has passed threshold(s) (true)
     * or has violated all or some of them (false).
     */
    private Map<BlockchainMetric, Boolean> thresholdValidationResults = new HashMap<>();

    public BlockchainMetaData getBlockchain() {
        return blockchain;
    }

    public void setBlockchain(BlockchainMetaData blockchain) {
        this.blockchain = blockchain;
    }

    public Map<BlockchainMetric, Integer> getWeightedScores() {
        return weightedScores;
    }

    public void setWeightedScores(Map<BlockchainMetric, Integer> weightedScores) {
        this.weightedScores = weightedScores;
    }

    public Map<BlockchainMetric, Boolean> getThresholdValidationResults() {
        return thresholdValidationResults;
    }

    public void setThresholdValidationResults(Map<BlockchainMetric, Boolean> thresholdValidationResults) {
        this.thresholdValidationResults = thresholdValidationResults;
    }

    public int getOverallWeightedScore() {
        return weightedScores
                .values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

}
