package com.ieee19.bc.interop.pf.core;

import com.ieee19.bc.interop.pf.core.model.Block;
import io.reactivex.*;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This RxJava operator removes every 5 seconds old blocks from the list and emits the remaining blocks. The timespan
 * defines the time a block should remain in the list.
 */
public class TimeEvictionBuffer implements FlowableTransformer<Block, List<Block>> {

    private static final Logger LOG = LoggerFactory.getLogger(TimeEvictionBuffer.class);

    private long timespan;

    private ChronoUnit timeUnit;

    private List<Block> blocks;

    private boolean timerTaskStarted = false;

    public TimeEvictionBuffer(long timespan, ChronoUnit timeUnit) {
        this.timespan = timespan;
        this.timeUnit = timeUnit;
        blocks = new ArrayList<>();
    }

    @Override
    public Publisher<List<Block>> apply(Flowable<Block> upstream) {
        return Flowable
                .create(emitter -> upstream.subscribe(
                        nextBlock -> {
                            startTimerTask(emitter);
                            addBlock(nextBlock);
                        },
                        throwable -> emitter.onError(throwable),
                        () -> emitter.onComplete()
                ), BackpressureStrategy.BUFFER);
    }

    private void addBlock(Block newBlock) {
        synchronized (blocks) {
            blocks.add(newBlock);
        }
    }

    private synchronized void startTimerTask(FlowableEmitter<List<Block>> emitter) {
        TimerTask timerTask;
        Timer timer;

        if (!timerTaskStarted) {
            timerTaskStarted = true;
            timer = new Timer("Timer");
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    synchronized (blocks) {
                        List<Block> survivedBlocks = blocks
                                .stream()
                                .parallel()
                                .filter(block -> block.getTimestamp().isAfter(ZonedDateTime.now().minus(timespan, timeUnit)))
                                .collect(Collectors.toList());

                        emitter.onNext(new ArrayList<>(survivedBlocks));
                        blocks = survivedBlocks;
                    }
                }
            };
            timer.schedule(timerTask, 5000, 5000);
        }
    }
}
