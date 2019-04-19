package com.ieee19.bc.interop.pf.proxy.ethereum;

import com.ieee19.bc.interop.pf.core.model.Block;
import com.ieee19.bc.interop.pf.core.IMetricCollector;
import com.ieee19.bc.interop.pf.core.TimeEvictionBuffer;
import com.ieee19.bc.interop.pf.proxy.currency.Currency;
import com.ieee19.bc.interop.pf.proxy.currency.interfaces.ICryptocurrencyPriceService;
import com.ieee19.bc.interop.pf.proxy.ethereum.interfaces.IEthereumService;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.groupingBy;

/**
 * This class represents the MetricCollector for Ethereum based blockchains and monitors supported metrics.
 */
public abstract class AbstractEthereumMetricCollector implements IMetricCollector {

    static final Logger LOG = LoggerFactory.getLogger(AbstractEthereumMetricCollector.class);

    private IEthereumService ethereumService;
    private ICryptocurrencyPriceService currencyPriceService;
    private Observable<List<Block>> blockObservable;
    private Observable<Double> avgBlockTimeObservable;
    private Observable<Double> transactionThroughputObservable;
    private Observable<Map<String, Double>> blockPercentagePerMinerObservable;
    private Observable<Double> networkHashrateObservable;
    private Observable<BigDecimal> exchangeRateObservable;
    private Observable<Long> gasPriceObservable;
    private Observable<BigDecimal> costsForWritingDataObservable;
    private Observable<BigDecimal> costsForRetrievingDataObservable;
    private Observable<BigDecimal> storageFeeObservable;
    private AtomicLong gasPrice = new AtomicLong(0);
    private Currency fiatCurrency;
    private int threadPoolSize;

    /**
     * @param ethereumService an instance of {@link IEthereumService}
     * @param priceService an instance of {@link ICryptocurrencyPriceService}
     * @param fiatCurrency the fiat currency to use for the calculation of exchange rates and costs
     * @param threadPoolSize the size of the thread pool to use for the initialization (download) of all blocks that
     *                       have been mined during the last 24 hours.
     */
    public AbstractEthereumMetricCollector(IEthereumService ethereumService, ICryptocurrencyPriceService priceService,
                                           Currency fiatCurrency, int threadPoolSize) {
        this.ethereumService = ethereumService;
        this.currencyPriceService = priceService;
        this.fiatCurrency = fiatCurrency;
        this.threadPoolSize = threadPoolSize;

        blockObservable = createBlockObservable();
        avgBlockTimeObservable = createAvgBlockTimeObservable();
        transactionThroughputObservable = createTransactionThroughputObservable();
        blockPercentagePerMinerObservable = createBlockPercentagePerMinerObservable();
        networkHashrateObservable = createNetworkHashrateObservable();
        exchangeRateObservable = createExchangeRateObservable();
        gasPriceObservable = createGasPriceObservable();
        costsForWritingDataObservable = createCostsForWritingDataObservable();
        costsForRetrievingDataObservable = createCostsForRetrievingDataObservable();
        storageFeeObservable = createStorageFeeObservable();
    }

    private Observable<List<Block>> createBlockObservable() {
        AtomicLong currentBlockHeight = new AtomicLong(0);

        Flowable<Block> mostRecentBlocksObservable = Flowable
                .create(emitter -> {
                    LOG.info("Collecting all blocks that have been mined during the last 24 hours.");
                    AtomicLong nextBlockHeightToFetch = new AtomicLong(ethereumService.getCurrentBlockNumber());
                    currentBlockHeight.set(nextBlockHeightToFetch.get());
                    ZonedDateTime nowMinus24h = ZonedDateTime.now().minus(24, ChronoUnit.HOURS);
                    Runnable fetchBlocksTask = () -> {
                        try {
                            do {
                                long nextBlockNumber = nextBlockHeightToFetch.getAndDecrement();
                                LOG.debug("Next block: " + nextBlockNumber);
                                Optional<Block> blockOpt = ethereumService.getBlockByNumberWithUncles(nextBlockNumber);
                                if (blockOpt.isPresent()) {
                                    Block block = blockOpt.get();
                                    if (block.getTimestamp().isAfter(nowMinus24h)) {
                                        synchronized (emitter) {
                                            emitter.onNext(blockOpt.get());
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            } while (true);
                        } catch (Throwable throwable) {
                            LOG.error(throwable.getMessage(), throwable);
                            emitter.tryOnError(throwable);
                        }
                    };
                    try {

                        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
                        IntStream
                                .range(0, threadPoolSize)
                                .forEach(i -> executorService.submit(fetchBlocksTask));  // submit task multiple times

                        executorService.shutdown();
                        if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
                            LOG.warn("Can't shutdown thread pool within 2 minutes");
                        }

                        LOG.info("Retrieved " + (currentBlockHeight.get() - nextBlockHeightToFetch.get()) + " blocks.");
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
                        long newBlockHeight = ethereumService.getCurrentBlockNumber();
                        long start = currentBlockHeight.get() + 1;
                        long count = newBlockHeight - start + 1;

                        if (count >= 0) {
                            currentBlockHeight.set(newBlockHeight);
                            Flowable
                                    .rangeLong(start, count)
                                    .onBackpressureBuffer()
                                    .map(blockNumber -> ethereumService.getBlockByNumberWithUncles(blockNumber))
                                    .filter(optional -> optional.isPresent())
                                    .map(optional -> optional.get())
                                    .subscribe(
                                            newBlock -> emitter.onNext(newBlock),
                                            error -> emitter.onError(error),
                                            () -> emitter.onComplete()
                                    );
                        } else {
                            LOG.error("start=" + start + ", count=" + count + ", newBlockHeight=" + newBlockHeight);
                        }
                    } catch (Throwable throwable) {
                        emitter.tryOnError(throwable);
                    }

                }, BackpressureStrategy.BUFFER)
                .repeatWhen(observable -> observable.delay(10, TimeUnit.SECONDS))
                .map(obj -> (Block) obj);

        return Flowable.concat(mostRecentBlocksObservable, continuousBlockObservable)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io(), false) // https://stackoverflow.com/questions/44920570/rxjava2-subscribe-stops-observing-after-a-while-but-continues-when-flowable-comp
                .observeOn(Schedulers.computation())
                .compose(new TimeEvictionBuffer(24, ChronoUnit.HOURS))
                .toObservable()
                .share()            // multicast
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

    private Observable<Map<String, Double>> createBlockPercentagePerMinerObservable() {
        return blockObservable
                .map(blocks -> {
                    AtomicInteger numberOfUncleBlocks = new AtomicInteger();
                    Map<String, List<Block>> blocksPerMiner = blocks
                            .stream()
                            .flatMap(block -> {
                                List<Block> blockAndUncleBlocks = new ArrayList<>(block.getUncleBlocks());
                                numberOfUncleBlocks.addAndGet(blockAndUncleBlocks.size());
                                blockAndUncleBlocks.add(block);
                                return blockAndUncleBlocks.stream();
                            })
                            .collect(groupingBy(Block::getMinerAddress));

                    return blocksPerMiner.entrySet()
                            .stream()
                            .parallel()
                            .map(entry -> new AbstractMap.SimpleEntry<>(
                                    entry.getKey(), 100.0 * entry.getValue().size() / (blocks.size() + numberOfUncleBlocks.get())
                            ))
                            .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
                });
    }

    private Observable<Double> createNetworkHashrateObservable() {
        return blockObservable
                .map(blocks -> blocks
                        .stream()
                        .parallel()
                        .mapToDouble(block -> block.getDifficulty() +
                                block.getUncleBlocks().stream().mapToDouble(Block::getDifficulty).sum())
                        .sum() / (24.0 * 3600)
                );
    }

    private Observable<BigDecimal> createExchangeRateObservable() {
        return Observable
                .create(emitter -> {
                    try {
                        BigDecimal price = currencyPriceService.getPrice(getCryptocurrency(), fiatCurrency);
                        emitter.onNext(price);
                        emitter.onComplete();
                    } catch (Throwable throwable) {
                        emitter.tryOnError(throwable);
                    }
                })
                .repeatWhen(observable -> observable.delay(10, TimeUnit.SECONDS))
                .map(price -> (BigDecimal) price)
                .share()            // multicast
                .replay(1)
                .autoConnect();
    }

    private Observable<Long> createGasPriceObservable() {
        return Observable
                .create(emitter -> {
                    if (gasPrice.get() == 0) {
                        try {
                            Long gasPrice = ethereumService.getGasPrice();
                            emitter.onNext(gasPrice);
                        } catch (Throwable throwable) {
                            emitter.tryOnError(throwable);
                        }
                    } else {
                        emitter.onNext(gasPrice.get());
                    }
                    emitter.onComplete();
                })
                .repeatWhen(observable -> observable.delay(2, TimeUnit.MINUTES))
                .map(priceInfo -> (Long) priceInfo)
                .share()            // multicast
                .replay(1)
                .autoConnect();
    }

    private Observable<BigDecimal> createCostsForWritingDataObservable() {
        return Observable
                .combineLatest(exchangeRateObservable, gasPriceObservable, (ethPriceInFiat, gasPrice) -> {
                    BigDecimal gasUsagePerKB = new BigDecimal(21000 + 68 * 1024);  // 21000 for every transaction, 68 for every non-zero byte
                    BigDecimal weiPriceInFiat = ethPriceInFiat.divide(BigDecimal.TEN.pow(18));
                    LOG.debug("Fiat price for 1 EXP: " + ethPriceInFiat + ", " + gasPrice.toString());
                    BigDecimal priceInFiat = gasUsagePerKB
                            .multiply(new BigDecimal(gasPrice)
                                    .multiply(weiPriceInFiat));
                    return priceInFiat;
                });
    }

    private Observable<BigDecimal> createCostsForRetrievingDataObservable() {
        return exchangeRateObservable
                .map(bitcoinPriceInFiat -> new BigDecimal(0)); // read operations are free
    }

    private Observable<BigDecimal> createStorageFeeObservable() {
        return exchangeRateObservable
                .map(bitcoinPriceInFiat -> new BigDecimal(0)); // currently there are no storage fees in Bitcoin

    }

    /**
     * @return the current set gas price in wei
     */
    public long getGasPrice() {
        return gasPrice.get();
    }

    /**
     * Sets the current gas price to <i>gasPriceInWei</i>
     * @param gasPriceInWei the gas price in wei to set
     */
    public void setGasPrice(long gasPriceInWei) {
        this.gasPrice.set(gasPriceInWei);
    }

    public abstract Currency getCryptocurrency();

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

    public Observable<Long> getGasPriceObservable() {
        return gasPriceObservable;
    }
}
