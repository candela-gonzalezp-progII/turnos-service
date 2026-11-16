package com.integrador.turnos.infrastructure.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceTokenProviderTest {

    private static final Instant NOW = Instant.parse("2026-11-05T12:00:00Z");
    private final HmacJwtService jwt = new HmacJwtService("un-secreto-de-prueba-bastante-largo-1234567890",
            "turnos-service", Clock.fixed(NOW, ZoneOffset.UTC));
    private final ServiceTokenProvider provider = new ServiceTokenProvider(jwt, Duration.ofSeconds(60));

    @Test
    void emiteTokenDeServicioDeVidaCorta() {
        JwtClaims claims = jwt.verify(provider.token("cande"));

        assertThat(claims.subject()).isEqualTo("turnos-service");
        assertThat(claims.authorities()).containsExactly("ROLE_SERVICE");
        assertThat(claims.onBehalfOf()).isEqualTo("cande");
        assertThat(claims.expiresAt()).isEqualTo(NOW.plusSeconds(60));
    }

    @Test
    void sinUsuarioEnCursoNoLlevaOnBehalfOf() {
        assertThat(jwt.verify(provider.token((String) null)).onBehalfOf() == null).isEqualTo(true);
    }
}
