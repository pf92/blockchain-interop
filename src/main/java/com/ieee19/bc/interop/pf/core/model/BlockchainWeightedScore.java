package com.ieee19.bc.interop.pf.core.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the weighted scores of a blockchain.
 */
public class BlockchainWeightedScore {

    private BlockchainMetaData blockchain;

    /**
     * Contains the weighted score for each blockchain metric.
     */
    private Map<BlockchainMetric, Integer> weightedMetricScores = new HashMap<>();

    public void setWeightedMetricScore(BlockchainMetric metric, int weightedScore) {
        weightedMetricScores.put(metric, weightedScore);
    }

    public Integer getWeightedMetricScore(BlockchainMetric metric) {
        return weightedMetricScores.get(metric);
    }

    public BlockchainMetaData getBlockchain() {
        return blockchain;
    }

    public Map<BlockchainMetric, Integer> getWeightedMetricScores() {
        return weightedMetricScores;
    }

    public void setWeightedMetricScores(Map<BlockchainMetric, Integer> weightedMetricScores) {
        this.weightedMetricScores = weightedMetricScores;
    }

    public void setBlockchain(BlockchainMetaData blockchain) {
        this.blockchain = blockchain;
    }

}
