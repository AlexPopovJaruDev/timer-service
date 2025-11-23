package com.jarudev.timer.monitor;

import com.jarudev.timer.properties.HealthProperties;
import com.jarudev.timer.repository.DbHealthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DbHealthMonitorTest {

    @Mock
    DbHealthRepository healthRepository;

    DbHealthMonitor monitor;

    @BeforeEach
    void setup() {
        HealthProperties props = new HealthProperties(0,1);
        monitor = new DbHealthMonitor(props, healthRepository);
    }

    @Test
    void initialState_shouldBeAvailable() {
        assertThat(monitor.isDbAvailable()).isTrue();
    }

    @Test
    void markDbAsUnavailable_shouldSwitchFlagAndStartHealthCheck() throws Exception {
        monitor.markDbAsUnavailable();

        assertThat(monitor.isDbAvailable()).isFalse();

        ScheduledFuture<?> task = getHealthTask();
        assertThat((Object) task).isNotNull();
        assertThat(task.isCancelled()).isFalse();
    }

    @Test
    void secondMarkDbAsUnavailable_shouldDoNothing() throws Exception {
        monitor.markDbAsUnavailable();
        ScheduledFuture<?> first = getHealthTask();

        monitor.markDbAsUnavailable();
        ScheduledFuture<?> second = getHealthTask();

        assertThat((Object)first).isSameAs(second);
    }

    @Test
    void checkDb_successfulPing_shouldMarkDbAsAvailableAndStopHealthCheck() throws Exception {
        monitor.markDbAsUnavailable();
        assertThat(monitor.isDbAvailable()).isFalse();

        invokeCheckDb();

        assertThat(monitor.isDbAvailable()).isTrue();
        ScheduledFuture<?> task = getHealthTask();
        assertThat((Object) task).isNull();
    }

    @Test
    void checkDb_failedPing_shouldKeepDbAsUnavailable() throws Exception {
        doThrow(new RuntimeException("DB down")).when(healthRepository).ping();

        monitor.markDbAsUnavailable();
        assertThat(monitor.isDbAvailable()).isFalse();

        invokeCheckDb();

        assertThat(monitor.isDbAvailable()).isFalse();
        assertThat((Object) getHealthTask()).isNotNull();
    }

    private ScheduledFuture<?> getHealthTask() throws Exception {
        var field = DbHealthMonitor.class.getDeclaredField("healthTask");
        field.setAccessible(true);
        return (ScheduledFuture<?>) field.get(monitor);
    }

    private void invokeCheckDb() throws Exception {
        Method m = DbHealthMonitor.class.getDeclaredMethod("checkDb");
        m.setAccessible(true);
        m.invoke(monitor);
    }
}