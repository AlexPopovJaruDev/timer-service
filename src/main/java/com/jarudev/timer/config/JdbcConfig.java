package com.jarudev.timer.config;

import com.jarudev.timer.properties.JdbcProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class JdbcConfig {

    //another comment

    private final JdbcProperties props;

    // измененный комментарий новая ветка
    // added 2 comment

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        template.setQueryTimeout(props.queryTimeout());
        return template;
    }
}
