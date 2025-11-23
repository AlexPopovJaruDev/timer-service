package com.jarudev.timer.exception;

import com.jarudev.timer.monitor.DbHealthMonitor;
import com.jarudev.timer.storage.TimeQueueStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InternalExceptionHandlerTest {

    @Mock
    private TimeQueueStorage queueStorage;

    @Mock
    private DbHealthMonitor dbHealthMonitor;

    @InjectMocks
    private InternalExceptionHandler handler;

    @Test
    void handleWriteFailure_whenConnectionProblem_shouldRequeueAndMarkDbUnavailable() {
        LocalDateTime t1 = LocalDateTime.now();
        LocalDateTime t2 = t1.plusSeconds(1);
        List<LocalDateTime> batch = List.of(t1, t2);

        SQLException sqlException = new SQLException("connection failed", "08001");
        DataAccessException dae =
                new DataAccessResourceFailureException("DB down", sqlException);

        handler.handleWriteFailure(batch, dae);

        verify(queueStorage).returnToHead(batch);
        verify(dbHealthMonitor).markDbAsUnavailable();
    }

    @Test
    void handleWriteFailure_whenNotConnectionProblem_shouldRethrowAndNotTouchQueueOrMonitor() {
        LocalDateTime t1 = LocalDateTime.now();
        List<LocalDateTime> batch = List.of(t1);

        SQLException sqlException = new SQLException("some other error", "23505");
        DataAccessException dae =
                new DataAccessResourceFailureException("Constraint violation", sqlException);

        assertThatThrownBy(() -> handler.handleWriteFailure(batch, dae))
                .isSameAs(dae);

        verifyNoInteractions(queueStorage);
        verifyNoInteractions(dbHealthMonitor);
    }
}