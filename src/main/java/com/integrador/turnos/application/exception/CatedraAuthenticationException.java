package com.integrador.turnos.application.exception;

/** La catedra rechazo el token tecnico (401, o 403 sin code): hay que volver a autenticarse. */
public class CatedraAuthenticationException extends RuntimeException {
    public CatedraAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
