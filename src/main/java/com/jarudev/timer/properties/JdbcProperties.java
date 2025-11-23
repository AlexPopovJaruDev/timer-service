package com.jarudev.timer.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "timer-service.jdbc")
public record JdbcProperties (int queryTimeout) {}

