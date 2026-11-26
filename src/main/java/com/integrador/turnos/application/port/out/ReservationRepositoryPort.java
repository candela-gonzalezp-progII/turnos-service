package com.integrador.turnos.application.port.out;

import com.integrador.turnos.domain.model.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepositoryPort {

    /** @throws com.integrador.turnos.application.exception.ConcurrentReservationUpdateException si otro cambio se adelanto */
    Reservation save(Reservation reservation);

    Optional<Reservation> findById(String id);

    /** Solo devuelve la reserva si pertenece a ese usuario. */
    Optional<Reservation> findByIdAndUser(String id, String userLogin);

    Optional<Reservation> findByCatedraProcessId(String catedraProcessId);

    /** Reserva del usuario sobre ese horario que sigue ocupandolo (pendiente o confirmada). */
    Optional<Reservation> findActiveForSlot(String userLogin, long professionalId, LocalDate date, LocalTime startTime);

    List<Reservation> findByUser(String userLogin);

    /** Procesos que todavia no terminaron (para recuperacion). */
    List<Reservation> findPending();
}
