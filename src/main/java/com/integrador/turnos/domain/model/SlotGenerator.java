package com.integrador.turnos.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Regla de dominio pura: dado un profesional y una fecha, genera los turnos de ese dia.
 * Un profesional deshabilitado o un horario deshabilitado no generan turnos.
 * El ultimo turno debe terminar antes o justo cuando termina la franja (no se genera un turno incompleto).
 */
public final class SlotGenerator {

    private SlotGenerator() {
    }

    public static List<Slot> generate(ProfessionalSummary professional, LocalDate date) {
        if (professional == null || date == null) {
            throw new IllegalArgumentException("profesional y fecha son obligatorios");
        }
        if (!professional.enabled()) {
            return List.of();
        }
        List<Slot> slots = new ArrayList<>();
        for (WorkSchedule schedule : professional.schedules()) {
            if (!schedule.enabled() || schedule.dayOfWeek() != date.getDayOfWeek()) {
                continue;
            }
            LocalTime start = schedule.startTime();
            while (!start.plusMinutes(schedule.slotDurationMinutes()).isAfter(schedule.endTime())
                    && start.plusMinutes(schedule.slotDurationMinutes()).isAfter(start)) {
                LocalTime end = start.plusMinutes(schedule.slotDurationMinutes());
                slots.add(new Slot(date, start, end));
                start = end;
            }
        }
        slots.sort(Comparator.comparing(Slot::start));
        return slots;
    }
}
