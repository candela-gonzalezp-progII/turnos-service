package com.integrador.turnos.infrastructure.catedra;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Datos de la catedra. Todo viene del .env (CATEDRA_*): nunca se escriben direcciones ni claves en el repo. */
@ConfigurationProperties(prefix = "catedra")
public record CatedraProperties(String baseUrl, String username, String password, Duration connectTimeout,
                                Duration readTimeout, int maxAttempts, Duration retryBackoff,
                                boolean kafkaEnabled, String kafkaConsumerGroupSuffix) {

    public CatedraProperties {
        connectTimeout = connectTimeout == null ? Duration.ofSeconds(3) : connectTimeout;
        readTimeout = readTimeout == null ? Duration.ofSeconds(10) : readTimeout;
        maxAttempts = maxAttempts <= 0 ? 3 : maxAttempts;
        retryBackoff = retryBackoff == null ? Duration.ofMillis(500) : retryBackoff;
        kafkaConsumerGroupSuffix = kafkaConsumerGroupSuffix == null ? "-turnos" : kafkaConsumerGroupSuffix;
    }
}
