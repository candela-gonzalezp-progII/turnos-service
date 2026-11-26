package com.integrador.turnos.application.service;

import com.integrador.turnos.application.exception.CatedraRejectedException;
import com.integrador.turnos.application.exception.HoldExpiredException;
import com.integrador.turnos.application.exception.InvalidSlotException;
import com.integrador.turnos.application.exception.ReservationNotFoundException;
import com.integrador.turnos.application.exception.SlotUnavailableException;
import com.integrador.turnos.application.port.in.ConfirmReservationUseCase;
import com.integrador.turnos.application.port.out.HoldConfirmationPort;
import com.integrador.turnos.application.port.out.ReservationRepositoryPort;
import com.integrador.turnos.application.port.out.UserRepositoryPort;
import com.integrador.turnos.domain.exception.InvalidReservationStateException;
import com.integrador.turnos.domain.model.PatientName;
import com.integrador.turnos.domain.model.Reservation;
import com.integrador.turnos.domain.model.ReservationStatus;
import com.integrador.turnos.domain.model.User;

import java.time.Clock;
import java.time.Instant;

/**
 * CU-11. Verifica que el proceso es del usuario y que el hold no vencio, confirma por REST en la catedra
 * y deja el proceso en CONFIRMATION_PENDING (esperando el pedido de telefono por Kafka).
 * Repetir el pedido (timeout) es idempotente: no duplica ni retrocede.
 */
public class ConfirmReservationService implements ConfirmReservationUseCase {

    private final ReservationRepositoryPort reservations;
    private final HoldConfirmationPort confirmations;
    private final UserRepositoryPort users;
    private final Clock clock;

    public ConfirmReservationService(ReservationRepositoryPort reservations, HoldConfirmationPort confirmations,
                                     UserRepositoryPort users, Clock clock) {
        this.reservations = reservations;
        this.confirmations = confirmations;
        this.users = users;
        this.clock = clock;
    }

    @Override
    public Reservation confirm(String userLogin, String reservationId, String firstName, String lastName) {
        Reservation reservation = reservations.findByIdAndUser(reservationId, userLogin)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        Instant now = clock.instant();

        switch (reservation.status()) {
            case CONFIRMATION_PENDING, INFO_REQUESTED, INFO_SUBMITTED, INFO_REJECTED, CONFIRMED -> {
                return reservation;                   // ya fue confirmada inicialmente: idempotente
            }
            case HOLD_CREATED -> { }
            default -> throw new InvalidReservationStateException(reservation.status(), ReservationStatus.CONFIRMATION_PENDING);
        }
        if (!reservation.isHoldValidAt(now)) {
            reservation.expire("HOLD_EXPIRED", now);
            reservations.save(reservation);
            throw new HoldExpiredException();
        }

        PatientName name = resolveName(userLogin, firstName, lastName);
        try {
            confirmations.confirmHold(reservation.holdId(), userLogin, name.firstName(), name.lastName());
        } catch (CatedraRejectedException e) {
            if (!e.hasCode("RESERVATION_PROCESS_ALREADY_CONFIRMED")) {
                handleRejection(reservation, e, now);   // siempre lanza
            }
        }
        return OptimisticRetry.run(() -> markStarted(reservationId, userLogin, name));
    }

    /** Recarga la reserva: un evento de Kafka puede haberla adelantado mientras esperabamos la respuesta REST. */
    private Reservation markStarted(String reservationId, String userLogin, PatientName name) {
        Reservation current = reservations.findByIdAndUser(reservationId, userLogin)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        if (current.status() == ReservationStatus.HOLD_CREATED) {
            current.confirmationStarted(name, clock.instant());
            return reservations.save(current);
        }
        return current;
    }

    private void handleRejection(Reservation reservation, CatedraRejectedException e, Instant now) {
        String code = e.code() == null ? "CATEDRA_REJECTED" : e.code();
        switch (code) {
            case "HOLD_EXPIRED", "HOLD_NOT_FOUND" -> {
                reservation.expire(code, now);
                reservations.save(reservation);
                throw new HoldExpiredException();
            }
            case "SLOT_ALREADY_RESERVED" -> {
                reservation.fail(code, now);
                reservations.save(reservation);
                throw new SlotUnavailableException(code);
            }
            case "INVALID_SLOT", "PROFESSIONAL_NOT_FOUND" -> {
                reservation.fail(code, now);
                reservations.save(reservation);
                throw new InvalidSlotException(code, "El horario no es valido para este profesional");
            }
            default -> throw e;
        }
    }

    private PatientName resolveName(String userLogin, String firstName, String lastName) {
        if (firstName != null && !firstName.isBlank() && lastName != null && !lastName.isBlank()) {
            return PatientName.of(firstName, lastName);
        }
        User account = users.findByLogin(userLogin).orElse(null);
        String first = firstName != null && !firstName.isBlank() ? firstName : account == null ? null : account.getFirstName();
        String last = lastName != null && !lastName.isBlank() ? lastName : account == null ? null : account.getLastName();
        return PatientName.of(first, last);
    }
}
