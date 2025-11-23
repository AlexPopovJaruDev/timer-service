package com.jarudev.timer.storage;

import com.jarudev.timer.properties.QueueProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TimeQueueStorageTest {

    private TimeQueueStorage storage;

    @BeforeEach
    void setup() {
        QueueProperties properties = new QueueProperties(3L);
        storage = new TimeQueueStorage(properties);
    }

    @Test
    void offer_ShouldAddElementsUntilLimit() {
        storage.offer(LocalDateTime.now());
        storage.offer(LocalDateTime.now());
        storage.offer(LocalDateTime.now());

        assertThat(storage.size()).isEqualTo(3);

        storage.offer(LocalDateTime.now());

        assertThat(storage.size()).isEqualTo(3);
    }

    @Test
    void drainOne_ShouldReturnElementsInOrder() {
        LocalDateTime t1 = LocalDateTime.now();
        LocalDateTime t2 = t1.plusSeconds(1);

        storage.offer(t1);
        storage.offer(t2);

        assertThat(storage.drainOne()).contains(t1);
        assertThat(storage.drainOne()).contains(t2);
        assertThat(storage.drainOne()).isEmpty();
    }

    @Test
    void drainUpTo_ShouldDrainUpToLimit() {
        LocalDateTime t1 = LocalDateTime.now();
        LocalDateTime t2 = t1.plusSeconds(1);
        LocalDateTime t3 = t1.plusSeconds(2);

        storage.offer(t1);
        storage.offer(t2);
        storage.offer(t3);

        List<LocalDateTime> drained = storage.drainUpTo(2);

        assertThat(drained).containsExactly(t1, t2);
        assertThat(storage.size()).isEqualTo(1);
    }

    @Test
    void returnToHead_ShouldInsertElementsOnTopInCorrectOrder() {
        LocalDateTime a = LocalDateTime.now();
        LocalDateTime b = a.plusSeconds(1);
        LocalDateTime c = a.plusSeconds(2);

        storage.offer(a);
        storage.offer(b);
        storage.offer(c);

        LocalDateTime x = a.minusSeconds(20);
        LocalDateTime y = a.minusSeconds(10);

        storage.returnToHead(List.of(x, y));

        assertThat(storage.drainUpTo(5))
                .containsExactly(x, y, a, b, c);
    }
}