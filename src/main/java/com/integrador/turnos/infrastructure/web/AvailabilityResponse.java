package com.integrador.turnos.infrastructure.web;

import com.integrador.turnos.domain.model.Availability;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Contrato publico de la disponibilidad. No expone clases de dominio. */
public record AvailabilityResponse(Long professionalId, String professionalName, String category,
                                   LocalDate date, List<SlotResponse> slots) {

    public record SlotResponse(LocalTime start, LocalTime end) {
    }

    public static AvailabilityResponse from(Availability a) {
        return new AvailabilityResponse(a.professional().id(), a.professional().fullName(),
                a.professional().categoryName(), a.date(),
                a.slots().stream().map(s -> new SlotResponse(s.start(), s.end())).toList());
    }
}
