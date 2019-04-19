package com.ieee19.bc.interop.pf.core;

import com.ieee19.bc.interop.pf.core.model.Block;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.SECONDS;

public class TimeEvictionBufferTest {

    private List<Block> createBlocks(int number, ZonedDateTime timestamp) {
        return IntStream
                .range(0, number)
                .mapToObj(i -> {
                    Block block = new Block();
                    block.setTimestamp(timestamp);
                    return block;
                })
                .collect(Collectors.toList());
    }

    @Test
    public void testBufferOutdated_shouldReturnEmptyList() {
        TimeEvictionBuffer buffer = new TimeEvictionBuffer(60, SECONDS);
        List<Block> blocks = createBlocks(5, ZonedDateTime.now().minus(65, SECONDS));
        TestSubscriber<List<Block>> testObserver = new TestSubscriber<>();
        Flowable
                .create(emitter -> {
                    blocks.stream().forEach(b -> emitter.onNext(b));
                    Thread.sleep(7000);
                }, BackpressureStrategy.BUFFER)
                .map(b -> (Block) b)
                .compose(buffer)
                .subscribe(testObserver);
        testObserver
                .assertValue(List::isEmpty)
                .assertNoErrors();
    }

    @Test
    public void testBufferSomeOutdated_shouldReturnOnlyYoungBlocks() {
        TimeEvictionBuffer buffer = new TimeEvictionBuffer(60, SECONDS);
        List<Block> blocks = createBlocks(5, ZonedDateTime.now().minus(70, SECONDS));
        blocks.addAll(createBlocks(3, ZonedDateTime.now()));
        TestSubscriber<List<Block>> testObserver = new TestSubscriber<>();
        Flowable
                .create(emitter -> {
                    blocks.stream().forEach(block -> emitter.onNext(block));
                    Thread.sleep(7000);
                    emitter.onComplete();
                }, BackpressureStrategy.BUFFER)
                .map(b -> (Block) b)
                .compose(buffer)
                .subscribe(testObserver);
        testObserver
                .assertValue(blockList -> blockList.size() == 3)
                .assertNoErrors();
    }

    @Test
    public void testBufferNoOutdated_shouldReturnAllBlocks() {
        TimeEvictionBuffer buffer = new TimeEvictionBuffer(60, SECONDS);
        List<Block> blocks = createBlocks(3, ZonedDateTime.now());
        TestSubscriber<List<Block>> testObserver = new TestSubscriber<>();
        Flowable
                .create(emitter -> {
                    blocks.stream().forEach(block -> emitter.onNext(block));
                    Thread.sleep(7000);
                    emitter.onComplete();
                }, BackpressureStrategy.BUFFER)
                .map(b -> (Block) b)
                .compose(buffer)
                .subscribe(testObserver);
        testObserver
                .assertValue(blockList -> blockList.size() == 3)
                .assertNoErrors();
    }

}
