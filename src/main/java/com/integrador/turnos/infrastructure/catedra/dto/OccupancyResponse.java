package com.integrador.turnos.infrastructure.catedra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.integrador.turnos.domain.model.Occupancy;
import com.integrador.turnos.domain.model.OccupancyStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OccupancyResponse(Long professionalId, LocalDate from, LocalDate to, List<Item> occupancies) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(LocalDate date, LocalTime startTime, String status, Instant expiresAt) {
    }

    public List<Occupancy> toDomain() {
        if (occupancies == null) {
            return List.of();
        }
        return occupancies.stream()
                .map(i -> new Occupancy(i.date(), i.startTime(), OccupancyStatus.valueOf(i.status()), i.expiresAt()))
                .toList();
    }
}
