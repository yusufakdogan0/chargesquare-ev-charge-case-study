package com.yusufakdogan.session_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestDatabaseConfig.class)
class SessionServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
