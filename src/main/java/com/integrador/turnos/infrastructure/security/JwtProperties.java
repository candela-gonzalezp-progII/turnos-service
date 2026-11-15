package com.integrador.turnos.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * security.jwt.secret  -> JWT_SECRET del .env (NUNCA en el repo; igual en catalogo y turnos)
 * security.jwt.issuer  -> quien emite (turnos-service)
 * security.jwt.expiration -> vida del token de usuario
 */
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(String secret, String issuer, Duration expiration, Duration serviceTokenTtl) {

    public JwtProperties {
        issuer = issuer == null || issuer.isBlank() ? "turnos-service" : issuer;
        expiration = expiration == null ? Duration.ofHours(1) : expiration;
        serviceTokenTtl = serviceTokenTtl == null ? Duration.ofSeconds(60) : serviceTokenTtl;
    }
}
