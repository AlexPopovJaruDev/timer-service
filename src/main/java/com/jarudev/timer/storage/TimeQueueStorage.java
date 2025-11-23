package com.jarudev.timer.storage;

import com.jarudev.timer.properties.QueueProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingDeque;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeQueueStorage {

    private final QueueProperties props;
    private final LinkedBlockingDeque<LocalDateTime> deque = new LinkedBlockingDeque<>();

    public void offer(LocalDateTime time) {
        if (deque.size() >= props.maxBufferSize()) {
            log.warn("Time buffer is full (max size = {}). Dropping entry {}", props.maxBufferSize(), time);
            return;
        }
        deque.offerLast(time);
    }

    public Optional<LocalDateTime> drainOne() {
        LocalDateTime ts = deque.pollFirst();
        return Optional.ofNullable(ts);
    }

    public List<LocalDateTime> drainUpTo(int batchSize) {
        List<LocalDateTime> result = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            LocalDateTime ts = deque.pollFirst();
            if (ts == null) {
                break;
            }
            result.add(ts);
        }
        return result;
    }

    public void returnToHead(List<LocalDateTime> list) {
        for (int i = list.size() - 1; i >= 0; i--) {
            deque.addFirst(list.get(i));
        }
        log.debug("Returned {} items back to the head of the queue", list.size());
    }

    public int size() {
        return deque.size();
    }
}
