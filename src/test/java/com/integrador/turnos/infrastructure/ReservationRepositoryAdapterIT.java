package com.integrador.turnos.infrastructure;

import com.integrador.turnos.AbstractPostgresIT;
import com.integrador.turnos.application.exception.ConcurrentReservationUpdateException;
import com.integrador.turnos.application.port.out.ReservationRepositoryPort;
import com.integrador.turnos.domain.model.Reservation;
import com.integrador.turnos.domain.model.ReservationStatus;
import com.integrador.turnos.support.Fixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Postgres real: mapeo, consultas, bloqueo optimista e indice unico del horario activo. */
class ReservationRepositoryAdapterIT extends AbstractPostgresIT {

    @Autowired
    ReservationRepositoryPort repo;

    @Test
    void guardaYRecuperaPorIdYUsuario() {
        Reservation saved = repo.save(Fixtures.heldReservation("it-user-1", "hold-it-1", "process-it-1"));
        assertThat(saved.version()).isEqualTo(0L);
        assertThat(repo.findByIdAndUser(saved.id(), "it-user-1").isPresent()).isTrue();
        assertThat(repo.findByIdAndUser(saved.id(), "otro").isPresent()).isFalse();
        assertThat(repo.findByCatedraProcessId("process-it-1").get().id()).isEqualTo(saved.id());
    }

    @Test
    void dosActualizacionesConLaMismaVersionFallanLaSegunda() {
        Reservation saved = repo.save(Fixtures.heldReservation("it-user-2", "hold-it-2", "process-it-2"));
        Reservation a = repo.findById(saved.id()).get();
        Reservation b = repo.findById(saved.id()).get();
        a.expire("X", Fixtures.NOW);
        repo.save(a);
        b.expire("Y", Fixtures.NOW);
        assertThatThrownBy(() -> repo.save(b)).isInstanceOf(ConcurrentReservationUpdateException.class);
    }

    @Test
    void noPermiteDosProcesosVivosDelMismoUsuarioEnElMismoHorario() {
        repo.save(Reservation.request("it-user-3", 15L, "Ana", Fixtures.MONDAY, LocalTime.of(9, 0), LocalTime.of(9, 30), Fixtures.NOW));
        var dup = Reservation.request("it-user-3", 15L, "Ana", Fixtures.MONDAY, LocalTime.of(9, 0), LocalTime.of(9, 30), Fixtures.NOW);
        assertThatThrownBy(() -> repo.save(dup)).isInstanceOf(ConcurrentReservationUpdateException.class);
    }

    @Test
    void unProcesoCerradoNoOcupaElHorario() {
        Reservation first = repo.save(Reservation.request("it-user-4", 15L, "Ana", Fixtures.MONDAY, LocalTime.of(9, 0), LocalTime.of(9, 30), Fixtures.NOW));
        first.fail("SLOT_ALREADY_HELD", Fixtures.NOW);
        repo.save(first);
        repo.save(Reservation.request("it-user-4", 15L, "Ana", Fixtures.MONDAY, LocalTime.of(9, 0), LocalTime.of(9, 30), Fixtures.NOW));
        assertThat(repo.findActiveForSlot("it-user-4", 15L, Fixtures.MONDAY, LocalTime.of(9, 0)).isPresent()).isTrue();
        assertThat(repo.findByUser("it-user-4")).hasSize(2);
        assertThat(repo.findPending().stream().anyMatch(r -> r.status() == ReservationStatus.HOLD_REQUESTED)).isTrue();
    }
}
