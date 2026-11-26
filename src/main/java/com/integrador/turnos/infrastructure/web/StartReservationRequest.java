package com.integrador.turnos.infrastructure.web;

import java.time.LocalDate;
import java.time.LocalTime;

public record StartReservationRequest(Long professionalId, LocalDate date, LocalTime startTime) {
}
