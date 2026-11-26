package com.integrador.turnos.application.service;

import com.integrador.turnos.application.exception.ReservationNotFoundException;
import com.integrador.turnos.application.port.in.GetReservationUseCase;
import com.integrador.turnos.application.port.out.ReservationRepositoryPort;
import com.integrador.turnos.domain.model.Reservation;

import java.time.Clock;
import java.time.Instant;

/** Estado de una reserva propia. Si el proceso vencio, lo cierra al consultarlo (EXPIRED). */
public class GetReservationService implements GetReservationUseCase {

    private final ReservationRepositoryPort reservations;
    private final Clock clock;

    public GetReservationService(ReservationRepositoryPort reservations, Clock clock) {
        this.reservations = reservations;
        this.clock = clock;
    }

    @Override
    public Reservation get(String userLogin, String reservationId) {
        Reservation reservation = reservations.findByIdAndUser(reservationId, userLogin)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        Instant now = clock.instant();
        if (reservation.isOverdueAt(now)) {
            return OptimisticRetry.run(() -> expire(reservationId, userLogin, now));
        }
        return reservation;
    }

    private Reservation expire(String reservationId, String userLogin, Instant now) {
        Reservation current = reservations.findByIdAndUser(reservationId, userLogin)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        if (current.isOverdueAt(now) && current.canTransitionTo(com.integrador.turnos.domain.model.ReservationStatus.EXPIRED)) {
            current.expire("HOLD_EXPIRED", now);
            return reservations.save(current);
        }
        return current;
    }
}
