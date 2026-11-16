package com.integrador.turnos;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
public abstract class AbstractPostgresIT {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        
        // Valores falsos de JWT para que levante el contexto de Spring en los tests
        registry.add("security.jwt.secret", () -> "este-es-un-secreto-falso-para-tests-muy-largo-1234567890");
        registry.add("security.jwt.issuer", () -> "turnos-service");
        registry.add("security.jwt.expiration", () -> "1h");
        registry.add("security.jwt.service-token-ttl", () -> "60s");
    }
}
