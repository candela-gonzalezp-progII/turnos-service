package com.integrador.turnos.application.service;

import com.integrador.turnos.application.exception.ProfessionalDisabledException;
import com.integrador.turnos.application.port.in.GetAvailabilityUseCase;
import com.integrador.turnos.application.port.out.CatalogPort;
import com.integrador.turnos.application.port.out.OccupancyPort;
import com.integrador.turnos.domain.model.Availability;
import com.integrador.turnos.domain.model.AvailabilityCalculator;
import com.integrador.turnos.domain.model.Occupancy;
import com.integrador.turnos.domain.model.ProfessionalSummary;
import com.integrador.turnos.domain.model.Slot;
import com.integrador.turnos.domain.model.SlotGenerator;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/** CU-09: agenda del catalogo (slots posibles) menos lo que la catedra informa como ocupado. */
public class AvailabilityService implements GetAvailabilityUseCase {

    private final CatalogPort catalog;
    private final OccupancyPort occupancies;
    private final Clock clock;
    private final ZoneId zone;

    public AvailabilityService(CatalogPort catalog, OccupancyPort occupancies, Clock clock, ZoneId zone) {
        this.catalog = catalog;
        this.occupancies = occupancies;
        this.clock = clock;
        this.zone = zone;
    }

    @Override
    public Availability getAvailability(long professionalId, LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        ProfessionalSummary p = catalog.findProfessional(professionalId);
        if (!p.enabled()) {
            throw new ProfessionalDisabledException(professionalId);
        }
        List<Slot> possible = SlotGenerator.generate(p, date);
        if (possible.isEmpty()) {
            return new Availability(p, date, possible);
        }
        List<Occupancy> busy = occupancies.findOccupancies(professionalId, date, date);
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), zone);
        return new Availability(p, date, AvailabilityCalculator.freeSlots(possible, busy, now));
    }
}
