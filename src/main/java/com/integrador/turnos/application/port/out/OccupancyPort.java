package com.integrador.turnos.application.port.out;

import com.integrador.turnos.domain.model.Occupancy;

import java.time.LocalDate;
import java.util.List;

/** Puerto de salida: horarios ocupados (confirmados o bloqueados) segun la catedra. */
public interface OccupancyPort {

    List<Occupancy> findOccupancies(long professionalId, LocalDate from, LocalDate to);
}
