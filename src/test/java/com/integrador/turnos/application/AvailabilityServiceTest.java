package com.integrador.turnos.application;

import com.integrador.turnos.application.exception.CatalogUnavailableException;
import com.integrador.turnos.application.exception.ProfessionalNotFoundException;
import com.integrador.turnos.application.service.AvailabilityService;
import com.integrador.turnos.domain.model.Availability;
import com.integrador.turnos.domain.model.ProfessionalSummary;
import com.integrador.turnos.domain.model.WorkSchedule;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvailabilityServiceTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 2);
    private final ProfessionalSummary ana = new ProfessionalSummary(15L, "Ana Gomez", "Clinica medica", true,
            List.of(new WorkSchedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), 30, true)));

    @Test
    void consultaAlCatalogoYGeneraLosTurnosDelDia() {
        var service = new AvailabilityService(id -> ana);

        Availability result = service.getAvailability(15L, MONDAY);

        assertThat(result.professional().fullName()).isEqualTo("Ana Gomez");
        assertThat(result.slots()).hasSize(2);
    }

    @Test
    void propagaProfesionalInexistente() {
        var service = new AvailabilityService(id -> {
            throw new ProfessionalNotFoundException(id);
        });

        assertThatThrownBy(() -> service.getAvailability(99L, MONDAY)).isInstanceOf(ProfessionalNotFoundException.class);
    }

    @Test
    void propagaCatalogoCaido() {
        var service = new AvailabilityService(id -> {
            throw new CatalogUnavailableException("caido", null);
        });

        assertThatThrownBy(() -> service.getAvailability(15L, MONDAY)).isInstanceOf(CatalogUnavailableException.class);
    }

    @Test
    void exigeFecha() {
        var service = new AvailabilityService(id -> ana);

        assertThatThrownBy(() -> service.getAvailability(15L, null)).isInstanceOf(IllegalArgumentException.class);
    }
}
