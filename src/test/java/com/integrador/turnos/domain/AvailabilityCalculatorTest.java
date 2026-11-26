package com.integrador.turnos.domain;

import com.integrador.turnos.domain.model.AvailabilityCalculator;
import com.integrador.turnos.domain.model.Occupancy;
import com.integrador.turnos.domain.model.OccupancyStatus;
import com.integrador.turnos.domain.model.Slot;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvailabilityCalculatorTest {

    private static final LocalDate D = LocalDate.of(2026, 11, 23);
    private final List<Slot> slots = List.of(new Slot(D, LocalTime.of(9, 0), LocalTime.of(9, 30)),
            new Slot(D, LocalTime.of(9, 30), LocalTime.of(10, 0)));

    @Test
    void sinOcupacionesDevuelveTodo() {
        assertThat(AvailabilityCalculator.freeSlots(slots, List.of(), LocalDateTime.of(2026, 11, 1, 0, 0))).hasSize(2);
    }

    @Test
    void confirmadoYBloqueadoSeQuitan() {
        var busy = List.of(new Occupancy(D, LocalTime.of(9, 0), OccupancyStatus.CONFIRMED, null),
                new Occupancy(D, LocalTime.of(9, 30), OccupancyStatus.HELD, null));
        assertThat(AvailabilityCalculator.freeSlots(slots, busy, LocalDateTime.of(2026, 11, 1, 0, 0))).isEmpty();
    }

    @Test
    void ocupacionDeOtroDiaNoAfecta() {
        var busy = List.of(new Occupancy(D.plusDays(1), LocalTime.of(9, 0), OccupancyStatus.CONFIRMED, null));
        assertThat(AvailabilityCalculator.freeSlots(slots, busy, LocalDateTime.of(2026, 11, 1, 0, 0))).hasSize(2);
    }

    @Test
    void quitaLosQueYaPasaron() {
        assertThat(AvailabilityCalculator.freeSlots(slots, List.of(), LocalDateTime.of(2026, 11, 23, 9, 10)))
                .extracting(Slot::start).containsExactly(LocalTime.of(9, 30));
    }
}
