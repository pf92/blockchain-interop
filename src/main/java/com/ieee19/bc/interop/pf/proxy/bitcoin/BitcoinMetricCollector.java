package com.ieee19.bc.interop.pf.proxy.bitcoin;

import com.ieee19.bc.interop.pf.core.*;
import com.ieee19.bc.interop.pf.core.model.Block;
import com.ieee19.bc.interop.pf.proxy.bitcoin.dto.blockcypher.FeePerKbInfo;
import com.ieee19.bc.interop.pf.proxy.bitcoin.interfaces.IBitcoinService;
import com.ieee19.bc.interop.pf.proxy.currency.CryptocurrencyPriceService;
import com.ieee19.bc.interop.pf.proxy.currency.Currency;
import com.ieee19.bc.interop.pf.proxy.currency.interfaces.ICryptocurrencyPriceService;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.groupingBy;

/**
 * Implements the {@link IMetricCollector} interface and monitors the Bitcoin blockchain.
 */
public class BitcoinMetricCollector implements IMetricCollector {

    static final Logger LOG = LoggerFactory.getLogger(BitcoinMetricCollector.class);

    private ICryptocurrencyPriceService currencyPriceService;
    private Observable<List<Block>> blockObservable;
    private Observable<Double> avgBlockTimeObservable;
    private Observable<Double> transactionThroughputObservable;
    private Observable<BigDecimal> exchangeRateObservable;
    private Observable<BigDecimal> costsForWritingDataObservable;
    private Observable<BigDecimal> costsForRetrievingDataObservable;
    private Observable<BigDecimal> storageFeeObservable;
    private Observable<Long> feePerKbInfoObservable;
    private Observable<Map<String, Double>> blockPercentagePerMinerObservable;
    private Observable<Double> networkHashrateObservable;
    private AtomicLong feesPerKb = new AtomicLong(0);
    private IBitcoinService bitcoinService;
    private Currency fiatCurrency;
    private int threadPoolSize;

    /**
     * @param bitcoinService an instance of {@link IBitcoinService}
     * @param priceService an instance of {@link ICryptocurrencyPriceService}
     * @param fiatCurrency the fiat currency to use for the calculation of exchange rates and costs
     * @param threadPoolSize the size of the thread pool to use for the initialization (download) of all blocks that
     *                       have been mined during the last 24 hours.
     */
    public BitcoinMetricCollector(IBitcoinService bitcoinService, ICryptocurrencyPriceService priceService,
                                  Currency fiatCurrency, int threadPoolSize) {
        this.bitcoinService = bitcoinService;
        this.currencyPriceService = priceService;
        this.fiatCurrency = fiatCurrency;
        this.threadPoolSize = threadPoolSize;

        blockObservable = createBlockObservable();
        avgBlockTimeObservable = createAvgBlockTimeObservable();
        transactionThroughputObservable = createTransactionThroughputObservable();
        exchangeRateObservable = createExchangeRateObservable();
        feePerKbInfoObservable = createFeePerKbObservable();
        costsForRetrievingDataObservable = createCostsForRetrievingDataObservable();
        storageFeeObservable = createStorageFeeObservable();
        blockPercentagePerMinerObservable = createBlockPercentagePerMinerObservable();
        networkHashrateObservable = createNetworkHashrateObservable();
        costsForWritingDataObservable = createCostsForWritingDataObservable();
    }

    private Observable<List<Block>> createBlockObservable() {
        AtomicLong currentBlockHeight = new AtomicLong(0);

        Flowable<Block> mostRecentBlocksObservable = Flowable
                .create(emitter -> {
                    LOG.info("Collecting all blocks that have been mined during the last 24 hours.");
                    AtomicLong nextBlockHeightToFetch = new AtomicLong(bitcoinService.getCurrentBlockHeight());
                    currentBlockHeight.set(nextBlockHeightToFetch.get());
                    ZonedDateTime nowMinus24h = ZonedDateTime.now().minus(24, ChronoUnit.HOURS);
                    Runnable fetchBlocksTask = () -> {
                        try {
                            do {
                                long nextBlockNumber = nextBlockHeightToFetch.getAndDecrement();
                                LOG.debug("Next block: " + nextBlockNumber);
                                Block block = bitcoinService.getBlockByBlockNumber(nextBlockNumber);
                                if (block.getTimestamp().isAfter(nowMinus24h)) {
                                    synchronized (emitter) {
                                        emitter.onNext(block);
                                    }
                                } else {
                                    break;
                                }
                            } while (true);
                        } catch (Throwable throwable) {
                            LOG.error(throwable.getMessage(), throwable);
                            emitter.tryOnError(throwable);
                        }
                    };
                    ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
                    try {
                        // submit task NUMBER_OF_THREADS times
                        IntStream.range(0, threadPoolSize).forEach(i -> executorService.submit(fetchBlocksTask));

                        executorService.shutdown();
                        if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
                            LOG.warn("Can't shutdown thread pool within 2 minutes");
                        }
                        emitter.onComplete();
                        LOG.info("Collecting blocks of last 24 hours finished. Retrieved " +
                                (currentBlockHeight.get() - nextBlockHeightToFetch.get()) + " blocks.");
                    } catch (InterruptedException e) {
                        // ignore, since it is thrown if an error is emitted
                    } catch (Throwable throwable) {
                        LOG.error(throwable.getMessage(), throwable);
                        emitter.tryOnError(throwable);
                    } finally {
                        emitter.onComplete();
                    }
                }, BackpressureStrategy.BUFFER);

        Flowable<Block> continuousBlockObservable = Flowable
                .create(emitter -> {
                    LOG.info("Check for new blocks");
                    try {
                        long newBlockHeight = bitcoinService.getCurrentBlockHeight();
                        long start = currentBlockHeight.get() + 1;
                        long count = newBlockHeight - start + 1;
                        currentBlockHeight.set(newBlockHeight);

                        Flowable
                                .rangeLong(start, count)
                                .onBackpressureBuffer()
                                .map(bitcoinService::getBlockByBlockNumber)
                                .subscribe(
                                        newBlock -> emitter.onNext(newBlock),
                                        error -> emitter.onError(error),
                                        () -> emitter.onComplete()
                                );
                    } catch (Throwable e) {
                        emitter.tryOnError(e);
                    }
                }, BackpressureStrategy.BUFFER)
                .repeatWhen(observable -> observable.delay(10, TimeUnit.SECONDS))
                .map(obj -> (Block) obj);

        return Flowable
                .concat(
                        mostRecentBlocksObservable,
                        continuousBlockObservable
                )
                .subscribeOn(Schedulers.io(), false)
                .observeOn(Schedulers.computation())
                .compose(new TimeEvictionBuffer(24, ChronoUnit.HOURS))
                .toObservable()
                .share()    // multicast
                .replay(1)
                .autoConnect();
    }

    private Observable<Double> createAvgBlockTimeObservable() {
        return blockObservable
                .map(blocks -> (24.0 * 3600) / blocks.size());
    }

    private Observable<Double> createTransactionThroughputObservable() {
        return blockObservable
                .map(blocks -> blocks.stream()
                        .parallel()
                        .mapToInt(block -> block.getNumberOfTransactions())
                        .sum()
                )
                .map(sum -> sum / (24.0 * 3600));
    }

    private Observable<BigDecimal> createExchangeRateObservable() {
        return Observable
                .create(emitter -> {
                    BigDecimal price;
                    try {
                        price = currencyPriceService.getPrice(Currency.BITCOIN, fiatCurrency);
                        emitter.onNext(price);
                        emitter.onComplete();
                    } catch (Throwable throwable) {
                        emitter.tryOnError(throwable);
                    }
                })
                .repeatWhen(observable -> observable.delay(10, TimeUnit.SECONDS))
                .map(price -> (BigDecimal) price)
                .share()    // multicast
                .replay(1)
                .autoConnect();
    }

    private Observable<BigDecimal> createCostsForRetrievingDataObservable() {
        return Observable
                .combineLatest(exchangeRateObservable, feePerKbInfoObservable, (btcPriceInFiat, feePerKbInfo) -> {
                    return new BigDecimal(0);  // currently, data reads are free
                });
    }

    private Observable<BigDecimal> createStorageFeeObservable() {
        return Observable
                .combineLatest(exchangeRateObservable, feePerKbInfoObservable, (btcPriceInFiat, feePerKbInfo) -> {
                    return new BigDecimal(0);  // currently there are no storage fees in Bitcoin
                });
    }

    private Observable<Long> createFeePerKbObservable() {
        return Observable
                .create(emitter -> {
                    if (feesPerKb.get() == 0) {
                        try {
                            FeePerKbInfo feePerKbInfo = bitcoinService.getFeePerKbInfo();
                            emitter.onNext((long) feePerKbInfo.getMediumFeePerKb());
                        } catch (Throwable throwable) {
                            emitter.tryOnError(throwable);
                        }
                    }
                    else {
                        emitter.onNext(feesPerKb.get());
                    }
                    emitter.onComplete();
                })
                .repeatWhen(observable -> observable.delay(2, TimeUnit.MINUTES))
                .map(feePerKb -> (Long) feePerKb)
                .share()    // multicast
                .replay(1)
                .autoConnect();
    }

    private Observable<Map<String, Double>> createBlockPercentagePerMinerObservable() {
        return blockObservable
                .map(blocks -> {
                    Map<String, List<Block>> blocksPerMiner = blocks
                            .stream()
                            .collect(groupingBy(Block::getMinerAddress));

                    return blocksPerMiner.entrySet()
                            .stream()
                            .parallel()
                            .map(entry -> new AbstractMap.SimpleEntry<>(
                                    entry.getKey(), 100.0 * entry.getValue().size() / blocks.size()
                            ))
                            .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
                });
    }

    private Observable<Double> createNetworkHashrateObservable() {
        return blockObservable
                .map(blocks -> {
                            int foundBlocks = blocks.size();
                            int expectedBlocks = 144;  // 144 blocks are expected to be mined in 24 hours
                            BigDecimal difficulty = BigDecimal.valueOf(blocks.get(blocks.size() - 1).getDifficulty());
                            LOG.debug("calculate network hashrate: foundBlocks = " + foundBlocks + ", difficulty: " + difficulty);
                            BigDecimal hashrate = BigDecimal.valueOf(foundBlocks)
                                    .divide(BigDecimal.valueOf(expectedBlocks), 10, RoundingMode.HALF_UP)
                                    .multiply(difficulty)
                                    .multiply(BigDecimal.valueOf(Math.pow(2, 32)))
                                    .divide(BigDecimal.valueOf(600), 10, RoundingMode.HALF_UP);
                            LOG.debug("hashrate = " + hashrate);
                            return hashrate.doubleValue();
                        }
                );
    }

    private Observable<BigDecimal> createCostsForWritingDataObservable() {
        return Observable
                .combineLatest(exchangeRateObservable, feePerKbInfoObservable, (btcPriceInFiat, feePerKb) -> {
                    // 242 byte -> tx with 40 byte data (= max. data size per tx), 227 bytes -> tx with 24 bytes data
                    int overallTransactionsSizeInByte = 25 * 242 + 227;  // size for 26 transaction with 1 kb data
                    BigDecimal satoshiPriceInFiat = btcPriceInFiat.divide(BigDecimal.TEN.pow(8), 10, RoundingMode.HALF_UP);
                    BigDecimal overallTransactionSizeInKb = new BigDecimal(overallTransactionsSizeInByte).divide(
                            BigDecimal.valueOf(1024), 10, RoundingMode.HALF_UP);


                    return overallTransactionSizeInKb
                            .multiply(BigDecimal.valueOf(feePerKb))
                            .multiply(satoshiPriceInFiat);
                });
    }

    @Override
    public Observable<List<Block>> getBlockObservable() {
        return blockObservable;
    }

    @Override
    public Observable<Double> getAvgBlockTimeObservable() {
        return avgBlockTimeObservable;
    }

    @Override
    public Observable<Double> getTransactionThroughputObservable() {
        return transactionThroughputObservable;
    }

    @Override
    public Observable<Map<String, Double>> getBlockPercentagePerMinerObservable() {
        return blockPercentagePerMinerObservable;
    }

    @Override
    public Observable<Double> getNetworkHashrateObservable() {
        return networkHashrateObservable;
    }

    @Override
    public Observable<BigDecimal> getExchangeRateObservable() {
        return exchangeRateObservable;
    }

    @Override
    public Observable<BigDecimal> getCostsForWritingDataObservable() {
        return costsForWritingDataObservable;
    }

    @Override
    public Observable<BigDecimal> getCostsForRetrievingDataObservable() {
        return costsForRetrievingDataObservable;
    }

    @Override
    public Observable<BigDecimal> getStorageFeeObservable() {
        return storageFeeObservable;
    }

    public long getFeesPerKb() {
        return feesPerKb.get();
    }

    public void setFeesPerKb(long feesPerKb) {
        this.feesPerKb.set(feesPerKb);
    }

    /**
     * Only used for testing purposes.
     * @param args
     */
    public static void main(String[] args) {
        BitcoinMetricCollector metricCollector = new BitcoinMetricCollector(
                new BitcoinService("http://35.242.216.4:3001/insight-api/"), new CryptocurrencyPriceService(), Currency.US_DOLLAR, 5
        );

        Runnable blockRunnable = () -> {
            while (!Thread.interrupted()) {
                metricCollector
                        .getBlockObservable()
                        .blockingSubscribe(lst -> LOG.info(lst.size() + " blocks have been minded during the last 24 hours"));
            }
        };
        Runnable blockTimeRunnable = () -> {
            while (!Thread.interrupted()) {
                metricCollector
                        .getAvgBlockTimeObservable()
                        .blockingSubscribe(blockTime -> LOG.info("Block time: " + blockTime + " s, " + blockTime / 60 + " m"));
            }
        };
        Runnable transactionThroughputRunnable = () -> {
            while (!Thread.interrupted()) {
                metricCollector
                        .getTransactionThroughputObservable()
                        .blockingSubscribe(transactionThroughput -> LOG.info("Transactions per second: " + transactionThroughput));
            }
        };
        Runnable exchangeRateRunnable = () -> {
            while (!Thread.interrupted()) {
                metricCollector
                        .getExchangeRateObservable()
                        .blockingSubscribe(priceInFiat -> LOG.info("Price for 1 BTC: " + priceInFiat));
            }
        };
        Runnable feePerKbInfoRunnable = () -> {
            while (!Thread.interrupted()) {
                metricCollector
                        .feePerKbInfoObservable
                        .blockingSubscribe(feePerKbInfo -> LOG.info("fee per kb info: " + feePerKbInfo));
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
                        .blockingSubscribe(hashrate -> LOG.info("Hashrate: " + String.format("%1$,.2f", hashrate)));
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        executorService.submit(blockRunnable);
        executorService.submit(blockTimeRunnable);
        executorService.submit(transactionThroughputRunnable);
        executorService.submit(exchangeRateRunnable);
        executorService.submit(feePerKbInfoRunnable);
        executorService.submit(costsForRetrievingDataRunnable);
        executorService.submit(storageFeeRunnable);
        executorService.submit(blockPercentagePerMinerRunnable);
        executorService.submit(networkHashrateRunnable);
        executorService.submit(costsForWritingDataRunnable);
    }
}
