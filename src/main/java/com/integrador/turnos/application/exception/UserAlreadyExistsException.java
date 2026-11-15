package com.integrador.turnos.application.exception;

/** El login o el email ya estan registrados. {@code field} dice cual (login o email). */
public class UserAlreadyExistsException extends RuntimeException {

    private final String field;

    public UserAlreadyExistsException(String field) {
        super("Ya existe un usuario con ese " + field);
        this.field = field;
    }

    public String field() {
        return field;
    }
}
