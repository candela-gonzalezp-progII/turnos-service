package com.integrador.turnos.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** Franja de atencion semanal de un profesional, tal como la publica catalogo-service. */
public record WorkSchedule(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
                           int slotDurationMinutes, boolean enabled) {

    public WorkSchedule {
        if (dayOfWeek == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("El horario necesita dia, inicio y fin");
        }
        if (slotDurationMinutes <= 0) {
            throw new IllegalArgumentException("La duracion del turno debe ser positiva");
        }
    }
}
