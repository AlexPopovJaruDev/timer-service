package com.jarudev.timer.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "timer-service.consumer")
public record ConsumerProperties(
        Duration emptyQueueSleep,
        Duration dbUnavailableSleep,
        int batchThreshold,
        int maxBatchSize) {
}
