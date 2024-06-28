package com.containerstore.common.base;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.math.BigDecimal.valueOf;

public class PerformanceMonitor {
    private static final long MILLIS_PER_SECOND = 1000L;
    private long threshold;
    private long startTime;
    private long stopTime;

    public PerformanceMonitor(long threshold) {
        this.threshold = threshold;
    }

    public PerformanceMonitor start() {
        startTime = System.currentTimeMillis();
        return this;
    }

    public PerformanceMonitor stop() {
        stopTime = System.currentTimeMillis();
        return this;
    }

    public long executionTime() {
        return stopTime - startTime;
    }

    public BigDecimal executionTimeInSeconds() {
        return valueOf(executionTime()).divide(valueOf(MILLIS_PER_SECOND), 2, RoundingMode.HALF_UP);
    }

    public boolean thresholdExceeded() {
        return executionTime() > threshold;
    }
}
