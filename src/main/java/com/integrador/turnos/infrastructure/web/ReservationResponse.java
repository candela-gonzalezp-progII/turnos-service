package com.integrador.turnos.infrastructure.web;

import com.integrador.turnos.domain.model.Reservation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/** Lo que ve la app. Nunca incluye el userLogin ni los ids internos de la catedra. */
public record ReservationResponse(String id, String status, long professionalId, String professionalName,
                                  LocalDate date, LocalTime startTime, LocalTime endTime, Instant expiresAt,
                                  boolean phoneRequired, String patientPhone, String reason, Instant createdAt,
                                  Instant updatedAt) {

    public static ReservationResponse from(Reservation r) {
        return new ReservationResponse(r.id(), r.status().name(), r.professionalId(), r.professionalName(),
                r.date(), r.startTime(), r.endTime(), r.isPending() ? r.holdExpiresAt() : null, r.requiresPhone(),
                r.patientPhone(), r.reason(), r.createdAt(), r.updatedAt());
    }
}
