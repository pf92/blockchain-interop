package com.ieee19.bc.interop.pf.core;

import com.ieee19.bc.interop.pf.core.model.Block;
import io.reactivex.Observable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Specifies an interface for obtaining blockchain metrics that is used by the {@link BlockchainManager} and must be
 * implemented for each blockchain that should be monitored.
 */
public interface IMetricCollector {

    /**
     * @return an {@link Observable} for getting the blocks of a blockchain.
     */
    Observable<List<Block>> getBlockObservable();

    /**
     * @return an {@link Observable} for getting the inter-block time that is calculated from the blocks that have been
     * mined during the last 24 hours.
     */
    Observable<Double> getAvgBlockTimeObservable();

    /**
     * @return an {@link Observable} for getting the transactions per second rate. This rate is calculated from the
     * blocks that have been mined during the last 24 hours.
     */
    Observable<Double> getTransactionThroughputObservable();

    /**
     * @return an {@link Observable} for getting mining distribution (percentage of mined blocks for each address)
     * that is calculated from the blocks that have been mined during the last 24 hours.
     */
    Observable<Map<String, Double>> getBlockPercentagePerMinerObservable();

    /**
     * @return an {@link Observable} for getting the network hashrate that is calculated from the blocks that have been
     * mined during the last 24 hours.
     */
    Observable<Double> getNetworkHashrateObservable();

    /**
     * @return an {@link Observable} for getting the amount of fiat currency for one unit of a cryptocurrency.
     */
    Observable<BigDecimal> getExchangeRateObservable();

    /**
     * @return an {@link Observable} for getting the costs in a fiat currency that have to be paid for writing 1 KB of
     * data to the blockchain.
     */
    Observable<BigDecimal> getCostsForWritingDataObservable();

    /**
     * @return an {@link Observable} for getting the costs in a fiat currency that have to be paid for reading 1 KB
     *                               data from the blockchain.
     */
    Observable<BigDecimal> getCostsForRetrievingDataObservable();

    /**
     * @return an {@link Observable} for getting the costs in a fiat currency that have to be paid as rental fees for
     *                               1 KB of data per hour.
     */
    Observable<BigDecimal> getStorageFeeObservable();

}
