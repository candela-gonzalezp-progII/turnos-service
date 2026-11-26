package com.integrador.turnos.application.port.in;

import com.integrador.turnos.domain.model.Reservation;

/** CU-15: cancelar una reserva propia confirmada. */
public interface CancelReservationUseCase {

    Reservation cancel(String userLogin, String reservationId, String reason);
}
