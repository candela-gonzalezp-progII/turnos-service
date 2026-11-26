package com.integrador.turnos.application.exception;

/** Otro proceso modifico la misma reserva al mismo tiempo (bloqueo optimista). */
public class ConcurrentReservationUpdateException extends RuntimeException {

    public ConcurrentReservationUpdateException(Throwable cause) {
        super("La reserva fue modificada en simultaneo", cause);
    }
}
