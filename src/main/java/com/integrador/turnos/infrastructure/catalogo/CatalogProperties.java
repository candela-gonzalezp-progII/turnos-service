package com.integrador.turnos.infrastructure.catalogo;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "catalogo")
public record CatalogProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {
}
