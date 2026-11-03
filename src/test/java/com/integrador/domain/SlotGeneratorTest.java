package com.integrador.domain;

import com.integrador.turnos.domain.model.ProfessionalSummary;
import com.integrador.turnos.domain.model.Slot;
import com.integrador.turnos.domain.model.SlotGenerator;
import com.integrador.turnos.domain.model.WorkSchedule;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlotGeneratorTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 2);

    private static ProfessionalSummary prof(boolean enabled, WorkSchedule... schedules) {
        return new ProfessionalSummary(15L, "Ana Gomez", "Clinica medica", enabled, List.of(schedules));
    }

    private static WorkSchedule monday(int start, int end, int minutes, boolean enabled) {
        return new WorkSchedule(DayOfWeek.MONDAY, LocalTime.of(start, 0), LocalTime.of(end, 0), minutes, enabled);
    }

    @Test
    void generaTurnosConsecutivosDentroDeLaFranja() {
        List<Slot> slots = SlotGenerator.generate(prof(true, monday(9, 11, 30, true)), MONDAY);

        assertThat(slots).hasSize(4);
        assertThat(slots.get(0).start()).isEqualTo(LocalTime.of(9, 0));
        assertThat(slots.get(0).end()).isEqualTo(LocalTime.of(9, 30));
        assertThat(slots.get(3).end()).isEqualTo(LocalTime.of(11, 0));
    }

    @Test
    void noGeneraUnTurnoIncompletoAlFinalDeLaFranja() {
        List<Slot> slots = SlotGenerator.generate(prof(true, monday(9, 10, 40, true)), MONDAY);

        assertThat(slots).hasSize(1);
    }

    @Test
    void otroDiaDeLaSemanaNoTieneTurnos() {
        assertThat(SlotGenerator.generate(prof(true, monday(9, 11, 30, true)), MONDAY.plusDays(1))).isEmpty();
    }

    @Test
    void unHorarioDeshabilitadoNoGeneraTurnos() {
        assertThat(SlotGenerator.generate(prof(true, monday(9, 11, 30, false)), MONDAY)).isEmpty();
    }

    @Test
    void unProfesionalDeshabilitadoNoGeneraTurnos() {
        assertThat(SlotGenerator.generate(prof(false, monday(9, 11, 30, true)), MONDAY)).isEmpty();
    }

    @Test
    void variasFranjasQuedanOrdenadasPorHora() {
        List<Slot> slots = SlotGenerator.generate(prof(true, monday(14, 15, 30, true), monday(9, 10, 30, true)), MONDAY);

        assertThat(slots).hasSize(4);
        assertThat(slots.get(0).start()).isEqualTo(LocalTime.of(9, 0));
        assertThat(slots.get(3).start()).isEqualTo(LocalTime.of(14, 30));
    }

    @Test
    void rechazaDuracionNoPositiva() {
        assertThatThrownBy(() -> new WorkSchedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), 0, true))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
