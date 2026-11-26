package com.integrador.turnos.infrastructure.catedra.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record HoldRequest(long professionalId, LocalDate date, LocalTime startTime, String externalPatientId) {
}
