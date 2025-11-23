package com.jarudev.timer;

import com.jarudev.timer.config.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TimerApplicationTests {

    @Test
    void contextLoads() {
    }
}
