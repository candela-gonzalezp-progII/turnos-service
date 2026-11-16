package com.integrador.turnos.infrastructure;

import com.integrador.turnos.application.exception.CatalogUnavailableException;
import com.integrador.turnos.application.exception.ProfessionalNotFoundException;
import com.integrador.turnos.application.service.AvailabilityService;
import com.integrador.turnos.domain.model.ProfessionalSummary;
import com.integrador.turnos.domain.model.WorkSchedule;
import com.integrador.turnos.infrastructure.web.ApiExceptionHandler;
import com.integrador.turnos.infrastructure.web.AvailabilityController;
import com.integrador.turnos.infrastructure.web.AvailabilityResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvailabilityControllerTest {

    private final ProfessionalSummary ana = new ProfessionalSummary(15L, "Ana Gomez", "Clinica medica", true,
            List.of(new WorkSchedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), 30, true)));
    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void devuelveLosTurnosDelDiaEnElContratoPublico() {
        var controller = new AvailabilityController(new AvailabilityService(id -> ana));

        AvailabilityResponse response = controller.availability(15L, LocalDate.of(2026, 11, 2));

        assertThat(response.professionalName()).isEqualTo("Ana Gomez");
        assertThat(response.slots()).hasSize(2);
        assertThat(response.slots().get(0).start()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    void profesionalInexistenteEs404ConCodigoEstable() {
        var response = handler.notFound(new ProfessionalNotFoundException(99L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getProperties().get("code")).isEqualTo("PROFESSIONAL_NOT_FOUND");
    }

    @Test
    void catalogoCaidoEs503SinDetallesInternos() {
        var response = handler.catalogDown(new CatalogUnavailableException("connect refused 10.0.0.5", null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().getProperties().get("code")).isEqualTo("CATALOG_UNAVAILABLE");
        assertThat(response.getBody().getDetail()).doesNotContain("10.0.0.5");
    }
}
