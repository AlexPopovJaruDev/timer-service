package com.jarudev.timer.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "timer-service.queue")
public record QueueProperties (Long maxBufferSize) {}

