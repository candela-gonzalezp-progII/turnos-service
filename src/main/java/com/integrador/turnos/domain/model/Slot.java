package com.integrador.turnos.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;

/** Un turno posible dentro de un dia. Todavia no hay reservas: todos figuran como disponibles. */
public record Slot(LocalDate date, LocalTime start, LocalTime end) {
}
