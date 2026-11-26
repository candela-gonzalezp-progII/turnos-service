package com.integrador.turnos.application.port.in;

import com.integrador.turnos.domain.model.Reservation;

/** CU-11: confirmar inicialmente el hold por REST. */
public interface ConfirmReservationUseCase {

    /** Nombre y apellido son opcionales: si faltan se usan los de la cuenta del usuario. */
    Reservation confirm(String userLogin, String reservationId, String firstName, String lastName);
}
