package com.integrador.turnos.domain.exception;

import com.integrador.turnos.domain.model.FieldError;

import java.util.List;

/** Datos invalidos. Lleva el detalle por campo (se informa al cliente como 400). */
public class ValidationException extends RuntimeException {

    private final List<FieldError> errors;

    public ValidationException(List<FieldError> errors) {
        super("Datos invalidos: " + errors.stream().map(FieldError::field).toList());
        this.errors = List.copyOf(errors);
    }

    public List<FieldError> errors() {
        return errors;
    }
}
