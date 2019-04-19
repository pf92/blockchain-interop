package com.ieee19.bc.interop.pf.core;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This RxJava operator emits true until false values have been received for the given time span, i.e. if only false
 * values arrived for the given time span, the operator emits false after the time span has elapsed. In case a true value
 * arrives after a series of false values and the time span is not elapsed, the operator resets its state and emits true.
 */
public class ThresholdViolationOperator implements ObservableTransformer<Boolean, Boolean> {

    static final Logger LOG = LoggerFactory.getLogger(ThresholdViolationOperator.class);

    private TimerTask timerTask;

    private Timer timer = new Timer("ThresholdViolationOperator");

    private AtomicBoolean lastEmitResult = new AtomicBoolean(true);

    private long timeSpan;

    private ChronoUnit timeUnit;

    public ThresholdViolationOperator(long timeSpan, ChronoUnit timeUnit) {
        this.timeSpan = timeSpan;
        this.timeUnit = timeUnit;
    }

    synchronized private void cancelTimerTask() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    synchronized private void scheduleTimerTask(ObservableEmitter<Object> emitter) {
        LOG.info("Schedule task");
        timerTask = new TimerTask() {
            @Override
            public void run() {
                emitter.onNext(false);
                lastEmitResult.set(false);
                timerTask = null;
            }
        };
        timer.schedule(timerTask, Duration.of(timeSpan, timeUnit).toMillis());
    }

    synchronized private boolean isTimerTaskScheduled() {
        return timerTask != null;
    }

    @Override
    public ObservableSource<Boolean> apply(Observable<Boolean> upstream) {
        return Observable
                .create(emitter -> upstream.subscribe(next -> {
                    if (next) {
//                        LOG.info("next: " + next);
//                        LOG.info("lastEmitResult: " + lastEmitResult.get());
                        if (!lastEmitResult.get()) {
                            // last emitted result was false -> no task is scheduled
                            lastEmitResult.set(true);
                            emitter.onNext(true);
                        } else if (isTimerTaskScheduled()) {
                            // last emitted result was true and a task is scheduled
                            LOG.info("cancel task: next=" + next + ", lastEmitResult=" + lastEmitResult.get());
                            cancelTimerTask();
                        }
                    } else {
                        if (!isTimerTaskScheduled() && lastEmitResult.get()) {
                            // last emitted result was true and no timer task is scheduled
                            if (timeSpan > 0) {
                                LOG.info("start task: next=" + next + ", lastEmitResult=" + lastEmitResult.get());
                                scheduleTimerTask(emitter);
                            }
                            else {
                                lastEmitResult.set(false);
                                emitter.onNext(false);
                            }
                        }
                    }
                }))
                .startWith(true)
                .map(b -> (Boolean) b);
    }

}
