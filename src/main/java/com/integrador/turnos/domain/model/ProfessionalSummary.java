package com.integrador.turnos.domain.model;

import java.util.List;

/** Lo que turnos-service necesita saber de un profesional (copia de lectura; la fuente es catalogo-service). */
public record ProfessionalSummary(long id, String fullName, String categoryName, boolean enabled,
                                  List<WorkSchedule> schedules) {

    public ProfessionalSummary {
        schedules = schedules == null ? List.of() : List.copyOf(schedules);
    }
}
