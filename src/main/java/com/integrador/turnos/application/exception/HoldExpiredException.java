package com.integrador.turnos.application.exception;

public class HoldExpiredException extends RuntimeException {

    public HoldExpiredException() {
        super("El bloqueo del horario vencio; inicie una nueva reserva");
    }
}
