package com.jarudev.timer.consumer;

import com.jarudev.timer.monitor.DbHealthMonitor;
import com.jarudev.timer.properties.ConsumerProperties;
import com.jarudev.timer.service.TimeService;
import com.jarudev.timer.storage.TimeQueueStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeConsumerTest {

    @Mock
    private TimeQueueStorage queueStorage;

    @Mock
    private TimeService timeService;

    @Mock
    private DbHealthMonitor dbHealthMonitor;

    private TimeConsumer consumer;

    @BeforeEach
    void setUp() {
        ConsumerProperties props = new ConsumerProperties(
                Duration.ZERO,
                Duration.ZERO,
                10,
                100);
        consumer = new TimeConsumer(queueStorage, timeService, dbHealthMonitor, props);
    }

    private void invokePollOnce() throws Exception {
        Method m = TimeConsumer.class.getDeclaredMethod("pollOnce");
        m.setAccessible(true);
        m.invoke(consumer);
    }

    @Test
    void pollOnce_dbUnavailable_shouldNotTouchQueueOrService() throws Exception {
        when(dbHealthMonitor.isDbAvailable()).thenReturn(false);

        invokePollOnce();

        verifyNoInteractions(queueStorage);
        verifyNoInteractions(timeService);
    }

    @Test
    void pollOnce_emptyQueue_shouldNotWriteAnything() throws Exception {
        when(dbHealthMonitor.isDbAvailable()).thenReturn(true);
        when(queueStorage.size()).thenReturn(0);

        invokePollOnce();

        verify(queueStorage).size();
        verifyNoMoreInteractions(queueStorage);
        verifyNoInteractions(timeService);
    }

    @Test
    void pollOnce_smallQueue_shouldWriteSingleElement() throws Exception {
        when(dbHealthMonitor.isDbAvailable()).thenReturn(true);
        when(queueStorage.size()).thenReturn(5);

        LocalDateTime ts = LocalDateTime.now();
        when(queueStorage.drainOne()).thenReturn(Optional.of(ts));

        invokePollOnce();

        InOrder inOrder = inOrder(queueStorage, timeService);
        inOrder.verify(queueStorage).size();
        inOrder.verify(queueStorage).drainOne();
        inOrder.verify(timeService).writeOne(ts);

        verifyNoMoreInteractions(timeService);
    }

    @Test
    void pollOnce_smallQueue_drainOneEmpty_shouldNotCallWriteOne() throws Exception {
        when(dbHealthMonitor.isDbAvailable()).thenReturn(true);
        when(queueStorage.size()).thenReturn(5);
        when(queueStorage.drainOne()).thenReturn(Optional.empty());

        invokePollOnce();

        verify(queueStorage).size();
        verify(queueStorage).drainOne();
        verifyNoInteractions(timeService);
    }

    @Test
    void pollOnce_largeQueue_shouldWriteBatch() throws Exception {
        when(dbHealthMonitor.isDbAvailable()).thenReturn(true);
        when(queueStorage.size()).thenReturn(20);

        LocalDateTime t1 = LocalDateTime.now();
        LocalDateTime t2 = t1.plusSeconds(1);
        List<LocalDateTime> batch = List.of(t1, t2);

        when(queueStorage.drainUpTo(100)).thenReturn(batch);

        invokePollOnce();

        InOrder inOrder = inOrder(queueStorage, timeService);
        inOrder.verify(queueStorage).size();
        inOrder.verify(queueStorage).drainUpTo(100);
        inOrder.verify(timeService).writeBatch(batch);

        verifyNoMoreInteractions(timeService);
    }

    @Test
    void pollOnce_largeQueue_emptyBatch_shouldNotCallWriteBatch() throws Exception {
        when(dbHealthMonitor.isDbAvailable()).thenReturn(true);
        when(queueStorage.size()).thenReturn(20);
        when(queueStorage.drainUpTo(100)).thenReturn(List.of());

        invokePollOnce();

        verify(queueStorage).size();
        verify(queueStorage).drainUpTo(100);
        verify(timeService, never()).writeBatch(anyList());
    }
}