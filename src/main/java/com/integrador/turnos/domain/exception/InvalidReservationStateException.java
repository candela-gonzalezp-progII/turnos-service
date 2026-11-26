package com.integrador.turnos.domain.exception;

import com.integrador.turnos.domain.model.ReservationStatus;

/** Se intento una transicion que la maquina de estados no permite. */
public class InvalidReservationStateException extends RuntimeException {

    private final ReservationStatus current;
    private final ReservationStatus target;

    public InvalidReservationStateException(ReservationStatus current, ReservationStatus target) {
        super("La reserva esta en estado " + current + " y no puede pasar a " + target);
        this.current = current;
        this.target = target;
    }

    public ReservationStatus current() {
        return current;
    }

    public ReservationStatus target() {
        return target;
    }
}
