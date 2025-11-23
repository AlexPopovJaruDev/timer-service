package com.jarudev.timer.monitor;

import com.jarudev.timer.properties.HealthProperties;
import com.jarudev.timer.repository.DbHealthRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class DbHealthMonitor {

    private final HealthProperties props;
    private final DbHealthRepository healthRepository;
    private final AtomicBoolean dbAvailable = new AtomicBoolean(true);
    private volatile ScheduledFuture<?> healthTask;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "db-health-monitor");
                t.setDaemon(true);
                return t;
            });

    public boolean isDbAvailable() {
        return dbAvailable.get();
    }

    public void markDbAsUnavailable() {
        if (dbAvailable.compareAndSet(true, false)) {
            log.warn("Marking DB as UNAVAILABLE. Starting health-check.");
            startHealthCheck();
        }
    }

    private void markDbAsAvailable() {
        if (dbAvailable.compareAndSet(false, true)) {
            log.info("DB is AVAILABLE again. Stopping health-check.");
            stopHealthCheck();
        }
    }

    private void startHealthCheck() {
        if (healthTask != null && !healthTask.isCancelled()) {
            return;
        }
        healthTask = scheduler.scheduleAtFixedRate(
                this::checkDb,
                props.initialDelay(),
                props.retryPeriod(),
                TimeUnit.SECONDS
        );
    }

    private void stopHealthCheck() {
        ScheduledFuture<?> localTask = this.healthTask;
        if (localTask != null) {
            localTask.cancel(false);
            this.healthTask = null;
        }
    }

    private void checkDb() {
        try {
            healthRepository.ping();
            markDbAsAvailable();
        } catch (Exception ex) {
            log.warn("DB health-check failed: {}", ex.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }
}
