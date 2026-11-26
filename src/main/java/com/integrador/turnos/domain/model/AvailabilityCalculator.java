package com.integrador.turnos.domain.model;

import java.time.LocalDateTime;
import java.util.List;

/** Slots posibles menos los ocupados por la catedra y menos los que ya pasaron. Puro, sin Spring. */
public final class AvailabilityCalculator {

    private AvailabilityCalculator() {
    }

    public static List<Slot> freeSlots(List<Slot> possible, List<Occupancy> occupancies, LocalDateTime now) {
        return possible.stream()
                .filter(s -> !LocalDateTime.of(s.date(), s.start()).isBefore(now))
                .filter(s -> occupancies.stream().noneMatch(o ->
                        o.date().equals(s.date()) && o.startTime().equals(s.start())))
                .toList();
    }
}
