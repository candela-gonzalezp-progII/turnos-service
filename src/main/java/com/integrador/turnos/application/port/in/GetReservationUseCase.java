package com.integrador.turnos.application.port.in;

import com.integrador.turnos.domain.model.Reservation;

/** Consulta el estado de una reserva propia (la app lo usa para seguir el proceso). */
public interface GetReservationUseCase {

    Reservation get(String userLogin, String reservationId);
}
