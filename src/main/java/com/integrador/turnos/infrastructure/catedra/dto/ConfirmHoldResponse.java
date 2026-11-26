package com.integrador.turnos.infrastructure.catedra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConfirmHoldResponse(String reservationProcessId, String status, Instant expiresAt) {
}
