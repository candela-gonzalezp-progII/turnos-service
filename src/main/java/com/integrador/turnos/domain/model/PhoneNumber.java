package com.integrador.turnos.domain.model;

import com.integrador.turnos.domain.exception.ValidationException;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Telefono del paciente. Igual que la catedra: se quitan espacios, guiones, parentesis y puntos y el resultado
 * debe cumplir ^\+?[0-9]{7,15}$. Se valida antes de publicar para no gastar un evento en un dato invalido.
 */
public record PhoneNumber(String value) {

    private static final Pattern VALID = Pattern.compile("^\\+?[0-9]{7,15}$");

    public static PhoneNumber of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException(List.of(new FieldError("phoneNumber", "es obligatorio")));
        }
        String cleaned = raw.replaceAll("[\\s\\-().]", "");
        if (!VALID.matcher(cleaned).matches()) {
            throw new ValidationException(List.of(new FieldError("phoneNumber",
                    "debe tener entre 7 y 15 digitos, con + opcional al inicio")));
        }
        return new PhoneNumber(cleaned);
    }

    /** Nunca se imprime completo (dato personal). */
    @Override
    public String toString() {
        return "PhoneNumber[***" + value.substring(Math.max(0, value.length() - 2)) + "]";
    }
}
