package com.integrador.turnos.application.service;

import com.integrador.turnos.application.port.in.HandleReservationEventUseCase;
import com.integrador.turnos.application.port.out.ProcessedEventPort;
import com.integrador.turnos.application.port.out.ReservationRepositoryPort;
import com.integrador.turnos.domain.model.EventOutcome;
import com.integrador.turnos.domain.model.Reservation;
import com.integrador.turnos.domain.model.ReservationEvent;
import com.integrador.turnos.domain.model.ReservationStatus;

import java.time.Clock;
import java.time.Instant;

/**
 * CU-13. Receptor idempotente de eventos de la catedra:
 *  1) ubica la reserva por reservationProcessId (si no existe no se registra el evento: puede llegar de nuevo);
 *  2) registra el eventId (si ya estaba, es duplicado y no se repiten efectos);
 *  3) aplica el cambio solo si la maquina de estados lo permite (un estado final no se reabre).
 * Corre dentro de UNA transaccion (decorador transaccional): si algo falla, el registro del eventId tambien
 * se deshace y el evento se reintenta.
 */
public class ReservationEventService implements HandleReservationEventUseCase {

    private final ReservationRepositoryPort reservations;
    private final ProcessedEventPort processed;
    private final Clock clock;

    public ReservationEventService(ReservationRepositoryPort reservations, ProcessedEventPort processed, Clock clock) {
        this.reservations = reservations;
        this.processed = processed;
        this.clock = clock;
    }

    @Override
    public EventOutcome handle(ReservationEvent event) {
        Reservation reservation = reservations.findByCatedraProcessId(event.reservationProcessId()).orElse(null);
        if (reservation == null) {
            return EventOutcome.UNKNOWN_PROCESS;
        }
        if (!processed.register(event.eventId())) {
            return EventOutcome.DUPLICATE;
        }
        Instant now = clock.instant();
        boolean applied = switch (event) {
            case ReservationEvent.InformationRequested e -> apply(reservation, ReservationStatus.INFO_REQUESTED,
                    () -> reservation.infoRequested(e.eventId(), e.expiresAt(), now));
            case ReservationEvent.Confirmed e -> apply(reservation, ReservationStatus.CONFIRMED,
                    () -> reservation.confirmed(e.reservationId(), now));
            case ReservationEvent.InformationRejected e -> apply(reservation, ReservationStatus.INFO_REJECTED,
                    () -> reservation.infoRejected(e.reason(), e.expiresAt(), now));
            case ReservationEvent.ProcessExpired e -> apply(reservation, ReservationStatus.EXPIRED,
                    () -> reservation.expire(orDefault(e.reason(), "HOLD_EXPIRED"), now));
            case ReservationEvent.ProcessInvalid e -> apply(reservation, ReservationStatus.INVALID,
                    () -> reservation.invalidate(orDefault(e.reason(), "INVALID"), now));
            case ReservationEvent.Cancelled e -> apply(reservation, ReservationStatus.CANCELLED,
                    () -> reservation.cancel(now));
        };
        if (!applied) {
            return EventOutcome.IGNORED;
        }
        reservations.save(reservation);
        return EventOutcome.APPLIED;
    }

    private static boolean apply(Reservation reservation, ReservationStatus target, Runnable change) {
        if (!reservation.canTransitionTo(target)) {
            return false;
        }
        change.run();
        return true;
    }

    private static String orDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
