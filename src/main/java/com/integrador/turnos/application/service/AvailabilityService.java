package com.integrador.turnos.application.service;

import com.integrador.turnos.application.port.in.GetAvailabilityUseCase;
import com.integrador.turnos.application.port.out.CatalogPort;
import com.integrador.turnos.domain.model.Availability;
import com.integrador.turnos.domain.model.ProfessionalSummary;
import com.integrador.turnos.domain.model.SlotGenerator;

import java.time.LocalDate;

public class AvailabilityService implements GetAvailabilityUseCase {

    private final CatalogPort catalog;

    public AvailabilityService(CatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    public Availability getAvailability(long professionalId, LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        ProfessionalSummary professional = catalog.findProfessional(professionalId);
        return new Availability(professional, date, SlotGenerator.generate(professional, date));
    }
}
