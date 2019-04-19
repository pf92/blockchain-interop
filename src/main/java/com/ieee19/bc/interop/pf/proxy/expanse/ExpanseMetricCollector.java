package com.ieee19.bc.interop.pf.proxy.expanse;

import com.ieee19.bc.interop.pf.core.IMetricCollector;
import com.ieee19.bc.interop.pf.proxy.currency.CryptocurrencyPriceService;
import com.ieee19.bc.interop.pf.proxy.currency.Currency;
import com.ieee19.bc.interop.pf.proxy.currency.interfaces.ICryptocurrencyPriceService;
import com.ieee19.bc.interop.pf.proxy.ethereum.AbstractEthereumMetricCollector;
import com.ieee19.bc.interop.pf.proxy.ethereum.exception.EthereumException;
import com.ieee19.bc.interop.pf.proxy.ethereum.interfaces.IEthereumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Implements the {@link IMetricCollector} interface and monitors the Expanse blockchain.
 */
public class ExpanseMetricCollector extends AbstractEthereumMetricCollector {

    private static final Logger LOG = LoggerFactory.getLogger(ExpanseMetricCollector.class);

    /**
     * @param ethereumService an instance of {@link IEthereumService}
     * @param priceService an instance of {@link ICryptocurrencyPriceService}
     * @param fiatCurrency the fiat currency to use for the calculation of exchange rates and costs
     * @param threadPoolSize the size of the thread pool to use for the initialization (download) of all blocks that
     *                       have been mined during the last 24 hours.
     */
    public ExpanseMetricCollector(IEthereumService ethereumService, ICryptocurrencyPriceService priceService,
                                  Currency fiatCurrency, int threadPoolSize) {
        super(ethereumService, priceService, fiatCurrency, threadPoolSize);
    }

//    @Override
//    public Observable<Double> getNetworkHashrateObservable() {
//        AtomicReference<Double> currentHashrate = new AtomicReference<>(210 * Math.pow(10, 9));
//        return Observable.create(emitter -> {
//            currentHashrate.updateAndGet(v -> (v - 5 * Math.pow(10, 9)));
//            LOG.info("Expanse network hashrate: " + currentHashrate.get() / Math.pow(10, 9) + " GH/s");
//            emitter.onNext(currentHashrate.get());
//            emitter.onComplete();
//        })
//                .repeatWhen(observable -> observable.delay(5, TimeUnit.SECONDS))
//                .map(obj -> (Double) obj)
//                .subscribeOn(Schedulers.single())
//                .observeOn(Schedulers.computation());
//    }
//    @Override
//    public Observable<BigDecimal> getCostsForWritingDataObservable() {
//        AtomicReference<Double> currentHashrate = new AtomicReference<>(-2.0);
//        return Observable.create(emitter -> {
//            currentHashrate.updateAndGet(v -> v+1);
//            LOG.info("Expanse writing costs: " + currentHashrate.get() + " USD");
//            emitter.onNext(currentHashrate.get());
//            emitter.onComplete();
//        })
//                .repeatWhen(observable -> observable.delay(5, TimeUnit.SECONDS))
//                .map(obj -> BigDecimal.valueOf((double) obj))
//                .subscribeOn(Schedulers.single())
//                .observeOn(Schedulers.computation());
//    }

    public static void main(String[] args) throws EthereumException {
        AbstractEthereumMetricCollector metricCollector = new ExpanseMetricCollector(
                new ExpanseService("http://35.198.77.10:9656", null),
                new CryptocurrencyPriceService(), Currency.US_DOLLAR, 10
        );
        Runnable blockRunnable = () -> {
            while (!Thread.interrupted()) {
                metricCollector
                        .getBlockObservable()
                        .blockingSubscribe(
                                lst -> LOG.info(lst.size() + " blocks have been minded during the last 24 hours"),
                                throwable -> {
                                    LOG.error("Error occurred: " + throwable.getMessage(), throwable);
                                    Thread.currentThread().interrupt();
                                }
                        );
            }
        };
        Runnable blockTimeRunnable = () -> {
            while (!Thread.interrupted()) {
                metricCollector
                        .getAvgBlockTimeObservable()
                        .blockingSubscribe(blockTime -> LOG.info("Block time: " + blockTime));
            }
        };
        Runnable transactionThroughputRunnable = () -> {
            while (!Thread.interrupted()) {
                metricCollector
                        .getTransactionThroughputObservable()
                        .blockingSubscribe(transactionThroughput -> LOG.info("Transactions per second: " + transactionThroughput));
            }
        };
        Runnable blockPercentagePerMinerRunnable = () -> {
            while (!Thread.interrupted()) {
                metricCollector
                        .getBlockPercentagePerMinerObservable()
                        .blockingSubscribe(percentagePerMiner -> {
                            Map<String, Double> sortedMap = percentagePerMiner.entrySet()
                                    .stream()
                                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
                            LOG.info("Percentage per miner: " + sortedMap);
                        });
            }
        };
        Runnable networkHashrateRunnable = () -> {
            while (!Thread.interrupted()) {
                metricCollector
                        .getNetworkHashrateObservable()
                        .blockingSubscribe(hashrate -> LOG.info("Hashrate: " + String.format("%1$,.2f", hashrate)
                                + " hashes per second"));
            }
        };
        Runnable ethPriceInFiatRunnable = () -> {
            while (!Thread.interrupted()) {
                metricCollector
                        .getExchangeRateObservable()
                        .blockingSubscribe(priceInFiat -> LOG.info("Price for 1 EXP: " + priceInFiat));
            }
        };
        Runnable gasPriceInfoRunnable = () -> {
            while (!Thread.interrupted()) {
                metricCollector
                        .getGasPriceObservable()
                        .blockingSubscribe(gasPrice -> LOG.info("gas price: " + gasPrice));
            }
        };
        Runnable costsForWritingDataRunnable = () -> {
            while (!Thread.interrupted()) {
                metricCollector.getCostsForWritingDataObservable()
                        .blockingSubscribe(costsForWritingData -> LOG.info("Costs for writing data: " + costsForWritingData));
            }
        };
        Runnable costsForRetrievingDataRunnable = () -> {
            while (!Thread.interrupted()) {
                metricCollector
                        .getCostsForRetrievingDataObservable()
                        .blockingSubscribe(costsForRetrievingData -> LOG.info("Costs for retrieving data: " + costsForRetrievingData));
            }
        };
        Runnable storageFeeRunnable = () -> {
            while (!Thread.interrupted()) {
                metricCollector
                        .getStorageFeeObservable()
                        .blockingSubscribe(storageFee -> LOG.info("Storage Fee: " + storageFee));
            }
        };
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        executorService.submit(blockRunnable);
//        executorService.submit(blockTimeRunnable);
//        executorService.submit(transactionThroughputRunnable);
//        executorService.submit(blockPercentagePerMinerRunnable);
//        executorService.submit(networkHashrateRunnable);
//        executorService.submit(ethPriceInFiatRunnable);
//        executorService.submit(gasPriceInfoRunnable);
//        executorService.submit(costsForWritingDataRunnable);
//        executorService.submit(costsForRetrievingDataRunnable);
//        executorService.submit(storageFeeRunnable);
    }

    @Override
    public Currency getCryptocurrency() {
        return Currency.EXPANSE;
    }

}
