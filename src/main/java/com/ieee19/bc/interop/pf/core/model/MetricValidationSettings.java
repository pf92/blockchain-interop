package com.ieee19.bc.interop.pf.core.model;

import com.ieee19.bc.interop.pf.core.BlockchainManager;

import java.time.temporal.ChronoUnit;
import java.util.function.Function;

/**
 * This class represents the metric vialidation settings that can be applied to the {@link BlockchainManager}.
 */
public class MetricValidationSettings<T> {

    private Function<T, Boolean> thresholdValidationFn;  // defines a valid range for a metric

    private long violationTimeSpan;  // defines time span a metric can violate the range until it is considered to be violated

    private ChronoUnit violationTimeUnit;

    public MetricValidationSettings() {
    }

    public MetricValidationSettings(Function<T, Boolean> thresholdValidationFn, long violationTimeSpan, ChronoUnit violationTimeUnit) {
        this.thresholdValidationFn = thresholdValidationFn;
        this.violationTimeSpan = violationTimeSpan;
        this.violationTimeUnit = violationTimeUnit;
    }

    public Function<T, Boolean> getThresholdValidationFn() {
        return thresholdValidationFn;
    }

    public void setThresholdValidationFn(Function<T, Boolean> thresholdValidationFn) {
        this.thresholdValidationFn = thresholdValidationFn;
    }

    public long getViolationTimeSpan() {
        return violationTimeSpan;
    }

    public void setViolationTimeSpan(long violationTimeSpan) {
        this.violationTimeSpan = violationTimeSpan;
    }

    public ChronoUnit getViolationTimeUnit() {
        return violationTimeUnit;
    }

    public void setViolationTimeUnit(ChronoUnit violationTimeUnit) {
        this.violationTimeUnit = violationTimeUnit;
    }

}
