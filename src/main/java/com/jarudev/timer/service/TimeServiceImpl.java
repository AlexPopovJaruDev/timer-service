package com.jarudev.timer.service;

import com.jarudev.timer.exception.InternalExceptionHandler;
import com.jarudev.timer.repository.TimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeServiceImpl implements TimeService {

    private final TimeRepository repository;
    private final InternalExceptionHandler handler;

    @Override
    public void writeOne(LocalDateTime dt) {
        try {
            repository.saveOne(dt);
        } catch (DataAccessException ex) {
            handler.handleWriteFailure(List.of(dt), ex);
        }
    }

    @Override
    public void writeBatch(List<LocalDateTime> dateTimes) {
        if (dateTimes.isEmpty()) {
            return;
        }
        try {
            repository.saveBatch(dateTimes);
        } catch (DataAccessException ex) {
            handler.handleWriteFailure(dateTimes, ex);
        }
    }

    @Override
    public List<LocalDateTime> findAll() {
        return repository.findAll();
    }
}
