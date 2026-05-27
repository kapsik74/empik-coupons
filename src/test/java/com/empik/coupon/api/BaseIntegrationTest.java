package com.empik.coupon.api;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    @ServiceConnection  // Spring Boot 3.1+ — samo konfiguruje datasource
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");
}
