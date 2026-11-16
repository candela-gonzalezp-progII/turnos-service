package com.integrador.turnos.application.exception;

public class ProfessionalNotFoundException extends RuntimeException {
    public ProfessionalNotFoundException(long id) {
        super("No existe el profesional " + id);
    }
}
