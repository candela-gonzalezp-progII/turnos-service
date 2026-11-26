package com.integrador.turnos.infrastructure.catedra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.integrador.turnos.domain.model.AppointmentSnapshot;
import com.integrador.turnos.domain.model.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

/** Una reserva de GET /api/appointments. Se descartan los datos personales del paciente. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AppointmentResponse(Long reservationId, String reservationProcessId, String externalPatientId,
                                  LocalDate appointmentDate, LocalTime startTime, String status) {

    public AppointmentSnapshot toDomain() {
        return new AppointmentSnapshot(reservationId, reservationProcessId, externalPatientId, appointmentDate,
                startTime, AppointmentStatus.valueOf(status));
    }
}
