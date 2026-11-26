package com.integrador.turnos.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "turnos")
public record TurnosProperties(String zone, boolean recoveryEnabled, Duration recoveryInterval,
                               Duration holdRequestTimeout) {

    public TurnosProperties {
        zone = zone == null || zone.isBlank() ? "America/Argentina/Buenos_Aires" : zone;
        recoveryInterval = recoveryInterval == null ? Duration.ofSeconds(60) : recoveryInterval;
        holdRequestTimeout = holdRequestTimeout == null ? Duration.ofMinutes(2) : holdRequestTimeout;
    }
}
