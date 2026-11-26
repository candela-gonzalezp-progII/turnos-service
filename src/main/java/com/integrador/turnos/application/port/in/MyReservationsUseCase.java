package com.integrador.turnos.application.port.in;

import com.integrador.turnos.domain.model.Reservation;

import java.util.List;

/** CU-14: reservas del usuario autenticado. */
public interface MyReservationsUseCase {

    MyReservations list(String userLogin);

    /** verified=false: la catedra no respondio y se muestran los datos locales sin contrastar. */
    record MyReservations(List<Reservation> reservations, boolean verified) {
    }
}
