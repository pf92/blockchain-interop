package com.ieee19.bc.interop.pf.core.model;

/**
 * This class represents a suggestion to switch to another blockchain.
 */
public class SwitchoverSuggestion {

    private BlockchainCalculationResult currentBlockchainResult;

    private BlockchainCalculationResult nextBlockchainResult;

    public BlockchainCalculationResult getCurrentBlockchainResult() {
        return currentBlockchainResult;
    }

    public void setCurrentBlockchainResult(BlockchainCalculationResult currentBlockchainResult) {
        this.currentBlockchainResult = currentBlockchainResult;
    }

    public BlockchainCalculationResult getNextBlockchainResult() {
        return nextBlockchainResult;
    }

    public void setNextBlockchainResult(BlockchainCalculationResult nextBlockchainResult) {
        this.nextBlockchainResult = nextBlockchainResult;
    }

}
