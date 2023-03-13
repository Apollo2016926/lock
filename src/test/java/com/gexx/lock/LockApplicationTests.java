package com.gexx.lock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@SpringBootTest
class LockApplicationTests {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
        jdbcTemplate.queryForList("select * from t_1");
    }

}
