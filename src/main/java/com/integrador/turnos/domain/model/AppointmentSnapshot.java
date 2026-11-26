package com.integrador.turnos.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;

/** Lo que la catedra informa de una reserva (sin datos personales del paciente: no los necesitamos). */
public record AppointmentSnapshot(Long reservationId, String reservationProcessId, String externalPatientId,
                                  LocalDate date, LocalTime startTime, AppointmentStatus status) {
}
