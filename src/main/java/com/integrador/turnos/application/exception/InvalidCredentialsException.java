package com.integrador.turnos.application.exception;

/** Credenciales incorrectas. El mensaje es generico a proposito: no revela si fallo el usuario o la clave. */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Credenciales invalidas");
    }
}
