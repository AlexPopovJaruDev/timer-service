package com.jarudev.timer.service;

import java.time.LocalDateTime;
import java.util.List;

public interface TimeService {

    void writeOne(LocalDateTime dateTime);

    void writeBatch(List<LocalDateTime> dateTimes);

    List<LocalDateTime> findAll();
}
