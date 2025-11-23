package com.jarudev.timer.consumer;

import com.jarudev.timer.monitor.DbHealthMonitor;
import com.jarudev.timer.properties.ConsumerProperties;
import com.jarudev.timer.service.TimeService;
import com.jarudev.timer.storage.TimeQueueStorage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeConsumer {

    private final TimeQueueStorage queueStorage;
    private final TimeService timeService;
    private final DbHealthMonitor dbHealthMonitor;
    private final ConsumerProperties props;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "time-consumer");
                t.setDaemon(true);
                return t;
            });

    @PostConstruct
    public void start() {
        executor.submit(this::run);
    }

    private void run() {
        log.info("TimeConsumer has been started.");
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    pollOnce();
                } catch (Exception ex) {
                    handleIterationError(ex);
                }
            }
        } finally {
            log.info("TimeConsumer stopped (thread interrupted).");
        }
    }

    private void pollOnce() {
        if (!dbHealthMonitor.isDbAvailable()) {
            await(props.dbUnavailableSleep());
            return;
        }

        int queueSize = queueStorage.size();
        if (queueSize == 0) {
            await(props.emptyQueueSleep());
            return;
        }

        if (queueSize < props.batchThreshold()) {
            writeSingle();
        } else {
            writeBatch();
        }
    }

    private void writeSingle() {
        queueStorage.drainOne().ifPresent(dateTime -> {
            timeService.writeOne(dateTime);
            log.debug("Wrote single timestamp {}", dateTime);
        });
    }

    private void writeBatch() {
        List<LocalDateTime> batch = queueStorage.drainUpTo(props.maxBatchSize());
        if (batch.isEmpty()) {
            return;
        }
        timeService.writeBatch(batch);
        log.debug("Wrote batch of {} timestamps", batch.size());
    }

    private void handleIterationError(Exception ex) {
        log.error("Error in TimeConsumer iteration", ex);
        if (!dbHealthMonitor.isDbAvailable()) {
            await(props.dbUnavailableSleep());
        }
    }

    private void await(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @PreDestroy
    public void stop() {
        executor.shutdownNow();
    }
}
