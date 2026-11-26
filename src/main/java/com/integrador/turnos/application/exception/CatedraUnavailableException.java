package com.integrador.turnos.application.exception;

/** La catedra no respondio (red, timeout, 5xx, 429). No prueba que la operacion haya fallado. */
public class CatedraUnavailableException extends RuntimeException {
    public CatedraUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
