package com.yusufakdogan.station_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestDatabaseConfig.class)
class StationServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
