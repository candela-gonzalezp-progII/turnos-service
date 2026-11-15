package com.integrador.turnos.domain.model;

/** Un error de validacion de un campo. Nunca incluye el valor recibido (puede ser una clave). */
public record FieldError(String field, String message) {
}
