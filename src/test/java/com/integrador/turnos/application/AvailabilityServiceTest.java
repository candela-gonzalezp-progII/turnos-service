package com.integrador.turnos.application;

import com.integrador.turnos.application.exception.CatalogUnavailableException;
import com.integrador.turnos.application.exception.CatedraUnavailableException;
import com.integrador.turnos.application.exception.ProfessionalDisabledException;
import com.integrador.turnos.application.exception.ProfessionalNotFoundException;
import com.integrador.turnos.application.service.AvailabilityService;
import com.integrador.turnos.domain.model.Availability;
import com.integrador.turnos.domain.model.Occupancy;
import com.integrador.turnos.domain.model.OccupancyStatus;
import com.integrador.turnos.domain.model.ProfessionalSummary;
import com.integrador.turnos.domain.model.WorkSchedule;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvailabilityServiceTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 23);
    private static final ZoneId ZONE = ZoneOffset.UTC;
    private final Clock before = Clock.fixed(Instant.parse("2026-11-01T00:00:00Z"), ZONE);
    private final ProfessionalSummary ana = new ProfessionalSummary(15L, "Ana Gomez", "Clinica medica", true,
            List.of(new WorkSchedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), 30, true)));

    private AvailabilityService service(ProfessionalSummary p, List<Occupancy> busy) {
        return new AvailabilityService(id -> p, (id, from, to) -> busy, before, ZONE);
    }

    @Test
    void generaLosTurnosDelDiaSiLaCatedraNoTieneOcupaciones() {
        Availability r = service(ana, List.of()).getAvailability(15L, MONDAY);
        assertThat(r.slots()).hasSize(2);
    }

    @Test
    void quitaLosHorariosOcupadosOBloqueadosPorLaCatedra() {
        var busy = List.of(new Occupancy(MONDAY, LocalTime.of(9, 0), OccupancyStatus.HELD, Instant.parse("2026-11-23T11:00:00Z")));
        Availability r = service(ana, busy).getAvailability(15L, MONDAY);
        assertThat(r.slots()).extracting(s -> s.start()).containsExactly(LocalTime.of(9, 30));
    }

    @Test
    void noOfreceHorariosQueYaPasaron() {
        Clock late = Clock.fixed(Instant.parse("2026-11-23T09:15:00Z"), ZONE);
        var s = new AvailabilityService(id -> ana, (id, f, t) -> List.of(), late, ZONE);
        assertThat(s.getAvailability(15L, MONDAY).slots()).extracting(x -> x.start()).containsExactly(LocalTime.of(9, 30));
    }

    @Test
    void profesionalDeshabilitadoNoTieneAgenda() {
        var off = new ProfessionalSummary(15L, "Ana Gomez", "Clinica medica", false, ana.schedules());
        assertThatThrownBy(() -> service(off, List.of()).getAvailability(15L, MONDAY))
                .isInstanceOf(ProfessionalDisabledException.class);
    }

    @Test
    void siNoHayHorariosPosiblesNoConsultaALaCatedra() {
        var s = new AvailabilityService(id -> ana, (id, f, t) -> { throw new AssertionError("no debia llamarse"); }, before, ZONE);
        assertThat(s.getAvailability(15L, LocalDate.of(2026, 11, 24)).slots()).isEmpty();
    }

    @Test
    void propagaProfesionalInexistente() {
        var s = new AvailabilityService(id -> { throw new ProfessionalNotFoundException(id); }, (id, f, t) -> List.of(), before, ZONE);
        assertThatThrownBy(() -> s.getAvailability(99L, MONDAY)).isInstanceOf(ProfessionalNotFoundException.class);
    }

    @Test
    void propagaCatalogoCaido() {
        var s = new AvailabilityService(id -> { throw new CatalogUnavailableException("caido", null); }, (id, f, t) -> List.of(), before, ZONE);
        assertThatThrownBy(() -> s.getAvailability(15L, MONDAY)).isInstanceOf(CatalogUnavailableException.class);
    }

    @Test
    void propagaCatedraCaida() {
        var s = new AvailabilityService(id -> ana, (id, f, t) -> { throw new CatedraUnavailableException("caida", null); }, before, ZONE);
        assertThatThrownBy(() -> s.getAvailability(15L, MONDAY)).isInstanceOf(CatedraUnavailableException.class);
    }

    @Test
    void fechaObligatoria() {
        assertThatThrownBy(() -> service(ana, List.of()).getAvailability(15L, null)).isInstanceOf(IllegalArgumentException.class);
    }
}
