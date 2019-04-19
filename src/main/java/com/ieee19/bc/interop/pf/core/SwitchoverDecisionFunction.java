package com.ieee19.bc.interop.pf.core;

/**
 * This interface represents a function that decides whether a switchover should be suggested (returns true) or not
 * (return false).
 */
@FunctionalInterface
public interface SwitchoverDecisionFunction {

    boolean apply(
            boolean costsForWritingDataValidationResult,
            boolean costsForRetrievingDataValidationResult,
            boolean storageFeesValidationResult,
            boolean exchangeRateValidationResult,
            boolean blockTimeValidationResult,
            boolean transactionThroughputValidationResult,
            boolean miningDistributionValidationResult,
            boolean networkHashrateValidationResult,
            boolean numberOfRequiredConfirmationsValidationResult,
            boolean reputationValidationResult
    );

}
