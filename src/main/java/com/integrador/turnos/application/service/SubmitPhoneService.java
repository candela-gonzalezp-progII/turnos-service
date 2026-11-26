package com.integrador.turnos.application.service;

import com.integrador.turnos.application.exception.HoldExpiredException;
import com.integrador.turnos.application.exception.ReservationNotFoundException;
import com.integrador.turnos.application.port.in.SubmitPhoneUseCase;
import com.integrador.turnos.application.port.out.PhonePublisherPort;
import com.integrador.turnos.application.port.out.ReservationRepositoryPort;
import com.integrador.turnos.domain.exception.InvalidReservationStateException;
import com.integrador.turnos.domain.model.PhoneNumber;
import com.integrador.turnos.domain.model.Reservation;
import com.integrador.turnos.domain.model.ReservationStatus;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * CU-12. Solo se acepta el telefono cuando la catedra lo pidio (INFO_REQUESTED) o lo rechazo (INFO_REJECTED, se
 * puede reintentar hasta el vencimiento). Se publica PRIMERO y se guarda DESPUES: si Kafka falla no queda un
 * estado "enviado" falso; si el guardado falla, el resultado igual llega por evento.
 */
public class SubmitPhoneService implements SubmitPhoneUseCase {

    private final ReservationRepositoryPort reservations;
    private final PhonePublisherPort publisher;
    private final Clock clock;

    public SubmitPhoneService(ReservationRepositoryPort reservations, PhonePublisherPort publisher, Clock clock) {
        this.reservations = reservations;
        this.publisher = publisher;
        this.clock = clock;
    }

    @Override
    public Reservation submit(String userLogin, String reservationId, String phoneNumber) {
        Reservation reservation = reservations.findByIdAndUser(reservationId, userLogin)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        Instant now = clock.instant();

        if (reservation.isOverdueAt(now)) {
            OptimisticRetry.run(() -> expire(reservationId, userLogin, now));
            throw new HoldExpiredException();
        }
        if (!reservation.requiresPhone()) {
            throw new InvalidReservationStateException(reservation.status(), ReservationStatus.INFO_SUBMITTED);
        }
        PhoneNumber phone = PhoneNumber.of(phoneNumber);

        publisher.publish(new PhonePublisherPort.PhoneSubmission("phone-" + UUID.randomUUID(),
                reservation.catedraProcessId(), reservation.holdId(), reservation.requestEventId(),
                phone.value(), now));

        return OptimisticRetry.run(() -> markSubmitted(reservationId, userLogin, phone, now));
    }

    /** Recarga: la confirmacion de la catedra puede haber llegado antes de guardar. Si ya avanzo, no se retrocede. */
    private Reservation markSubmitted(String id, String user, PhoneNumber phone, Instant now) {
        Reservation current = reservations.findByIdAndUser(id, user)
                .orElseThrow(() -> new ReservationNotFoundException(id));
        if (current.canTransitionTo(ReservationStatus.INFO_SUBMITTED)) {
            current.infoSubmitted(phone.value(), now);
            return reservations.save(current);
        }
        return current;
    }

    private Reservation expire(String id, String user, Instant now) {
        Reservation current = reservations.findByIdAndUser(id, user)
                .orElseThrow(() -> new ReservationNotFoundException(id));
        if (current.canTransitionTo(ReservationStatus.EXPIRED)) {
            current.expire("HOLD_EXPIRED", now);
            return reservations.save(current);
        }
        return current;
    }
}
