package com.integrador.turnos.domain.model;

import java.time.Instant;

/**
 * Eventos que la catedra publica en alumnos.turnos.acciones.{groupId}. Cada tipo es un record (Strategy por
 * tipo: el servicio decide con un switch exhaustivo, el compilador avisa si falta un caso).
 */
public sealed interface ReservationEvent {

    String eventId();

    String reservationProcessId();

    record InformationRequested(String eventId, String reservationProcessId, String holdId, Instant expiresAt)
            implements ReservationEvent {
    }

    record Confirmed(String eventId, String reservationProcessId, Long reservationId) implements ReservationEvent {
    }

    record InformationRejected(String eventId, String reservationProcessId, String reason, Instant expiresAt)
            implements ReservationEvent {
    }

    record ProcessExpired(String eventId, String reservationProcessId, String reason) implements ReservationEvent {
    }

    record ProcessInvalid(String eventId, String reservationProcessId, String reason) implements ReservationEvent {
    }

    record Cancelled(String eventId, String reservationProcessId) implements ReservationEvent {
    }
}
