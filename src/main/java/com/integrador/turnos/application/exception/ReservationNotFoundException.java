package com.integrador.turnos.application.exception;

/** No existe, o no pertenece al usuario autenticado (no se revela cual de las dos). */
public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException(String id) {
        super("No existe la reserva " + id);
    }
}
