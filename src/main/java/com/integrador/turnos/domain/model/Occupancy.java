package com.integrador.turnos.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/** Horario no disponible segun la catedra: confirmado o bloqueado (hold) hasta expiresAt. */
public record Occupancy(LocalDate date, LocalTime startTime, OccupancyStatus status, Instant expiresAt) {
}
