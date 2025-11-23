package com.jarudev.timer.repository;

import java.time.LocalDateTime;
import java.util.List;

public interface TimeRepository {

    void saveOne(LocalDateTime timestamp);

    void saveBatch(List<LocalDateTime> timestamps);

    List<LocalDateTime> findAll();
}
