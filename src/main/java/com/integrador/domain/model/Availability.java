package com.integrador.domain.model;

import java.time.LocalDate;
import java.util.List;

public record Availability(ProfessionalSummary professional, LocalDate date, List<Slot> slots) {

    public Availability {
        slots = List.copyOf(slots);
    }
}
