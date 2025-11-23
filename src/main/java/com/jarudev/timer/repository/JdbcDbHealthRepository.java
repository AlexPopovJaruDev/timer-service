package com.jarudev.timer.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcDbHealthRepository implements DbHealthRepository {

    private static final String PING_SQL =
            "SELECT 1";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void ping() {
        jdbcTemplate.queryForObject(PING_SQL, Integer.class);
    }
}
