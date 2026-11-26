package com.integrador.turnos.application.exception;

public class ReservationNotCancellableException extends RuntimeException {
    public ReservationNotCancellableException() {
        super("La reserva no se puede cancelar");
    }
}
