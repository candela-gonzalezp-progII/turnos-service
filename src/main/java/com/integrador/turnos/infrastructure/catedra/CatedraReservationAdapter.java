package com.integrador.turnos.infrastructure.catedra;

import com.integrador.turnos.application.port.out.OccupancyPort;
import com.integrador.turnos.domain.model.Occupancy;
import com.integrador.turnos.infrastructure.catedra.dto.OccupancyResponse;

import java.time.LocalDate;
import java.util.List;

/** Adapter de salida hacia la API REST de la catedra (ocupaciones, holds, confirmaciones, reservas). */
public class CatedraReservationAdapter implements OccupancyPort {

    private final CatedraHttp http;

    public CatedraReservationAdapter(CatedraHttp http) {
        this.http = http;
    }

    @Override
    public List<Occupancy> findOccupancies(long professionalId, LocalDate from, LocalDate to) {
        OccupancyResponse response = http.get(
                "/api/appointment-occupancies?professionalId={id}&from={from}&to={to}",
                OccupancyResponse.class, professionalId, from, to);
        return response.toDomain();
    }
}
