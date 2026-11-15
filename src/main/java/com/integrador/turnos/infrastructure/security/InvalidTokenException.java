package com.integrador.turnos.infrastructure.security;

/** Token ausente, mal formado, con firma invalida, vencido o de otro emisor. */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
