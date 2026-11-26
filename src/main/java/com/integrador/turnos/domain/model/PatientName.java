package com.integrador.turnos.domain.model;

import com.integrador.turnos.domain.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

/** Nombre y apellido del paciente: 2 a 100 caracteres cada uno (restriccion del contrato de la catedra). */
public record PatientName(String firstName, String lastName) {

    public static PatientName of(String firstName, String lastName) {
        List<FieldError> errors = new ArrayList<>();
        check("patientFirstName", firstName, errors);
        check("patientLastName", lastName, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        return new PatientName(firstName.trim(), lastName.trim());
    }

    private static void check(String field, String value, List<FieldError> errors) {
        if (value == null || value.isBlank()) {
            errors.add(new FieldError(field, "es obligatorio"));
        } else if (value.trim().length() < 2 || value.trim().length() > 100) {
            errors.add(new FieldError(field, "debe tener entre 2 y 100 caracteres"));
        }
    }
}
