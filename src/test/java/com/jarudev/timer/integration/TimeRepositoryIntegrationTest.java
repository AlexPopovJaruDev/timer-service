package com.jarudev.timer.integration;

import com.jarudev.timer.config.TestcontainersConfiguration;
import com.jarudev.timer.consumer.TimeConsumer;
import com.jarudev.timer.producer.TimeProducer;
import com.jarudev.timer.repository.TimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TimeRepositoryIntegrationTest {

    @MockitoBean
    private TimeConsumer timeConsumer;

    @MockitoBean
    private TimeProducer timeProducer;

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    //TODO: rebase
    @BeforeEach
    void cleanDb() {
        jdbcTemplate.update("DELETE FROM time_records");
    }

    @Test
    void saveOne_shouldPersistSingleTimestamp() {
        LocalDateTime ts = LocalDateTime.of(2025, 1, 1, 12, 0, 0);

        timeRepository.saveOne(ts);

        List<LocalDateTime> all = timeRepository.findAll();
        assertThat(all)
                .hasSize(1)
                .containsExactly(ts);
    }

    @Test
    void saveBatch_shouldPersistAllTimestamps() {
        LocalDateTime base = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
        List<LocalDateTime> batch = List.of(
                base,
                base.plusSeconds(1),
                base.plusSeconds(2)
        );

        timeRepository.saveBatch(batch);

        List<LocalDateTime> all = timeRepository.findAll();
        assertThat(all)
                .hasSize(batch.size())
                .containsExactlyElementsOf(batch);
    }

    @Test
    void findAll_shouldReturnSequentialSecondsWithoutGaps() {
        LocalDateTime base = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
        List<LocalDateTime> batch = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            batch.add(base.plusSeconds(i));
        }

        timeRepository.saveBatch(batch);
        List<LocalDateTime> fromDb = timeRepository.findAll();

        assertThat(fromDb).hasSize(batch.size());
        assertThat(fromDb.get(0)).isEqualTo(base);
        for (int i = 1; i < fromDb.size(); i++) {
            LocalDateTime prev = fromDb.get(i - 1);
            LocalDateTime curr = fromDb.get(i);
            Duration diff = Duration.between(prev, curr);
            assertThat(diff.getSeconds()).isEqualTo(1);
        }
    }
}
