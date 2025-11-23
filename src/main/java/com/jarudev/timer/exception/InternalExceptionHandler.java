package com.jarudev.timer.exception;

import com.jarudev.timer.monitor.DbHealthMonitor;
import com.jarudev.timer.storage.TimeQueueStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InternalExceptionHandler {

    private final TimeQueueStorage queueStorage;
    private final DbHealthMonitor dbHealthMonitor;

    public void handleWriteFailure(List<LocalDateTime> dateTimes, DataAccessException ex) {
        if (isConnectionProblem(ex)) {
            log.error("DB connection problem during write, re-queueing {} items", dateTimes.size(), ex);
            queueStorage.returnToHead(dateTimes);
            dbHealthMonitor.markDbAsUnavailable();
        } else {
            log.error("Unexpected DB error during write", ex);
            throw ex;
        }
    }

    private boolean isConnectionProblem(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof SQLException sqlEx) {
                String sqlState = sqlEx.getSQLState();
                if (sqlState != null && sqlState.startsWith("08")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}
