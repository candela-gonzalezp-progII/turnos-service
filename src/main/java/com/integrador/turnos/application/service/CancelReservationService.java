package com.integrador.turnos.application.service;

import com.integrador.turnos.application.exception.CatedraRejectedException;
import com.integrador.turnos.application.exception.ReservationNotCancellableException;
import com.integrador.turnos.application.exception.ReservationNotFoundException;
import com.integrador.turnos.application.port.in.CancelReservationUseCase;
import com.integrador.turnos.application.port.out.AppointmentCancellationPort;
import com.integrador.turnos.application.port.out.ReservationRepositoryPort;
import com.integrador.turnos.domain.exception.InvalidReservationStateException;
import com.integrador.turnos.domain.model.Reservation;
import com.integrador.turnos.domain.model.ReservationStatus;

import java.time.Clock;

/**
 * CU-15. Solo reservas propias y CONFIRMED. La cancelacion es idempotente: una reserva ya CANCELLED devuelve OK.
 * Si la catedra falla, el estado local no cambia (no se corrompe) y el usuario puede reintentar.
 */
public class CancelReservationService implements CancelReservationUseCase {

    private final ReservationRepositoryPort reservations;
    private final AppointmentCancellationPort catedra;
    private final Clock clock;

    public CancelReservationService(ReservationRepositoryPort reservations, AppointmentCancellationPort catedra, Clock clock) {
        this.reservations = reservations;
        this.catedra = catedra;
        this.clock = clock;
    }

    @Override
    public Reservation cancel(String userLogin, String reservationId, String reason) {
        if (reason != null && reason.length() > 500) {
            throw new IllegalArgumentException("El motivo admite hasta 500 caracteres");
        }
        Reservation reservation = reservations.findByIdAndUser(reservationId, userLogin)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        if (reservation.status() == ReservationStatus.CANCELLED) {
            return reservation;
        }
        if (reservation.status() != ReservationStatus.CONFIRMED) {
            throw new InvalidReservationStateException(reservation.status(), ReservationStatus.CANCELLED);
        }
        try {
            catedra.cancel(reservation.catedraProcessId(), reason);
        } catch (CatedraRejectedException e) {
            if (e.hasCode("RESERVATION_NOT_CANCELLABLE")) {
                throw new ReservationNotCancellableException();
            }
            throw e;
        }
        return OptimisticRetry.run(() -> markCancelled(reservationId, userLogin));
    }

    /** Recarga: el evento AppointmentCancelled puede haberse adelantado. */
    private Reservation markCancelled(String reservationId, String userLogin) {
        Reservation current = reservations.findByIdAndUser(reservationId, userLogin)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        if (current.status() == ReservationStatus.CONFIRMED) {
            current.cancel(clock.instant());
            return reservations.save(current);
        }
        return current;
    }
}
