package com.integrador.turnos.infrastructure.kafka;

/** El mensaje no se puede interpretar. Reintentarlo no sirve: se registra y se descarta. */
public class MalformedEventException extends RuntimeException {
    public MalformedEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
