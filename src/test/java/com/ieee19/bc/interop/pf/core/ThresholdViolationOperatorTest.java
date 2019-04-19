package com.ieee19.bc.interop.pf.core;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

public class ThresholdViolationOperatorTest {

    @Test
    public void test_onlyFalseValues_shouldReturnTrueAndFalse() {
        ThresholdViolationOperator operator = new ThresholdViolationOperator(3, ChronoUnit.SECONDS);
        TestObserver<Boolean> testObserver = new TestObserver<>();
        Observable
                .create(emitter -> {
                    emitter.onNext(false);
                    emitter.onNext(false);
                    Thread.sleep(1000);
                    emitter.onNext(false);
                    Thread.sleep(3000);
                    emitter.onComplete();
                })
                .map(b -> (Boolean) b)
                .compose(operator)
                .subscribe(testObserver);
        testObserver
                .assertValues(true, false)
                .assertNoErrors();
    }

    @Test
    public void test_onlyTrueValues_shouldReturnOnlyTrue() {
        ThresholdViolationOperator operator = new ThresholdViolationOperator(3, ChronoUnit.SECONDS);
        TestObserver<Boolean> testObserver = new TestObserver<>();
        Observable
                .create(emitter -> {
                    emitter.onNext(true);
                    emitter.onNext(true);
                    Thread.sleep(1000);
                    emitter.onNext(true);
                    Thread.sleep(3000);
                    emitter.onComplete();
                })
                .map(b -> (Boolean) b)
                .compose(operator)
                .subscribe(testObserver);
        testObserver
                .assertValue(true)
                .assertNoErrors();
    }

    @Test
    public void test_taskSchedulerCancelation_shouldReturnTrue() {
        ThresholdViolationOperator operator = new ThresholdViolationOperator(3, ChronoUnit.SECONDS);
        TestObserver<Boolean> testObserver = new TestObserver<>();
        Observable
                .create(emitter -> {
                    emitter.onNext(true);
                    emitter.onNext(false);
                    Thread.sleep(1000);
                    emitter.onNext(true);
                    Thread.sleep(3000);
                    emitter.onComplete();
                })
                .map(b -> (Boolean) b)
                .compose(operator)
                .subscribe(testObserver);
        testObserver
                .assertValue(true)
                .assertNoErrors();
    }

    @Test
    public void test_taskSchedulerCancelationAndReScheduling_shouldReturnTrueAndFalse() {
        ThresholdViolationOperator operator = new ThresholdViolationOperator(3, ChronoUnit.SECONDS);
        TestObserver<Boolean> testObserver = new TestObserver<>();
        Observable
                .create(emitter -> {
                    emitter.onNext(true);
                    emitter.onNext(false);
                    Thread.sleep(1000);
                    emitter.onNext(true);
                    Thread.sleep(1000);
                    emitter.onNext(false);
                    Thread.sleep(4000);
                    emitter.onComplete();
                })
                .map(b -> (Boolean) b)
                .compose(operator)
                .subscribe(testObserver);
        testObserver
                .assertValues(true, false)
                .assertNoErrors();
    }

    @Test
    public void test_2taskSchedulerCancelationsAndReScheduling_shouldReturnTrueAndTrueAndFalse() {
        ThresholdViolationOperator operator = new ThresholdViolationOperator(3, ChronoUnit.SECONDS);
        TestObserver<Boolean> testObserver = new TestObserver<>();
        Observable
                .create(emitter -> {
                    emitter.onNext(true);
                    emitter.onNext(false);
                    Thread.sleep(1000);
                    emitter.onNext(true);
                    emitter.onNext(false);
                    emitter.onNext(false);
                    emitter.onNext(false);
                    Thread.sleep(500);
                    emitter.onNext(true);
                    emitter.onNext(false);
                    emitter.onNext(false);
                    Thread.sleep(4000);
                    emitter.onComplete();
                })
                .map(b -> (Boolean) b)
                .compose(operator)
                .subscribe(testObserver);
        testObserver
                .assertValues(true, false)
                .assertNoErrors();
    }

    @Test
    public void test_noValues_shouldReturnTrue() {
        ThresholdViolationOperator operator = new ThresholdViolationOperator(3, ChronoUnit.SECONDS);
        TestObserver<Boolean> testObserver = new TestObserver<>();
        Observable
                .create(emitter -> {
                    Thread.sleep(1000);
                    emitter.onComplete();
                })
                .map(b -> (Boolean) b)
                .compose(operator)
                .subscribe(testObserver);
        testObserver
                .assertValues(true)
                .assertNoErrors();
    }

}
