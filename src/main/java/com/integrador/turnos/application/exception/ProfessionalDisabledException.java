package com.integrador.turnos.application.exception;

public class ProfessionalDisabledException extends RuntimeException {
    public ProfessionalDisabledException(long professionalId) {
        super("El profesional " + professionalId + " no esta habilitado");
    }
}
