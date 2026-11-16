package com.integrador.turnos.application.port.in;

import com.integrador.turnos.domain.model.Availability;

import java.time.LocalDate;

public interface GetAvailabilityUseCase {

    Availability getAvailability(long professionalId, LocalDate date);
}
