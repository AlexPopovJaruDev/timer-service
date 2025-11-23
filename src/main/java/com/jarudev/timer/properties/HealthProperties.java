package com.jarudev.timer.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "timer-service.health")
public record HealthProperties(
        int initialDelay,
        int retryPeriod
) {}
