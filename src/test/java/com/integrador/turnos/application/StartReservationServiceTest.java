package com.integrador.turnos.application;

import com.integrador.turnos.application.exception.CatedraRejectedException;
import com.integrador.turnos.application.exception.CatedraUnavailableException;
import com.integrador.turnos.application.exception.InvalidSlotException;
import com.integrador.turnos.application.exception.ProfessionalDisabledException;
import com.integrador.turnos.application.exception.SlotUnavailableException;
import com.integrador.turnos.application.port.out.HoldPort;
import com.integrador.turnos.application.service.StartReservationService;
import com.integrador.turnos.domain.model.ProfessionalSummary;
import com.integrador.turnos.domain.model.Reservation;
import com.integrador.turnos.domain.model.ReservationStatus;
import com.integrador.turnos.support.FakeReservationRepository;
import com.integrador.turnos.support.Fixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StartReservationServiceTest {

    private final FakeReservationRepository repo = new FakeReservationRepository();
    private int holdCalls = 0;

    private HoldPort okHold() {
        return (pid, date, start, patient) -> {
            holdCalls++;
            return new HoldPort.HoldResult("hold-" + holdCalls, "process-" + holdCalls, Fixtures.NOW.plusSeconds(900));
        };
    }

    private StartReservationService service(HoldPort holds, ProfessionalSummary p) {
        return new StartReservationService(id -> p, holds, repo, Fixtures.CLOCK, ZoneOffset.UTC);
    }

    @Test
    void creaElProcesoYPideElHoldALaCatedra() {
        Reservation r = service(okHold(), Fixtures.ANA).start("juan", 15L, Fixtures.MONDAY, LocalTime.of(9, 0));
        assertThat(r.status()).isEqualTo(ReservationStatus.HOLD_CREATED);
        assertThat(r.holdId()).isEqualTo("hold-1");
        assertThat(r.userLogin()).isEqualTo("juan");
        assertThat(r.endTime()).isEqualTo(LocalTime.of(9, 30));
        assertThat(repo.all()).hasSize(1);
    }

    @Test
    void repetirElPedidoDevuelveElMismoProcesoSinDuplicarElHold() {
        var s = service(okHold(), Fixtures.ANA);
        Reservation a = s.start("juan", 15L, Fixtures.MONDAY, LocalTime.of(9, 0));
        Reservation b = s.start("juan", 15L, Fixtures.MONDAY, LocalTime.of(9, 0));
        assertThat(b.id()).isEqualTo(a.id());
        assertThat(holdCalls).isEqualTo(1);
    }

    @Test
    void siElProcesoAnteriorVencioSeCreaUnoNuevo() {
        var s = service(okHold(), Fixtures.ANA);
        Reservation a = s.start("juan", 15L, Fixtures.MONDAY, LocalTime.of(9, 0));
        var later = new StartReservationService(id -> Fixtures.ANA, okHold(), repo,
                java.time.Clock.fixed(Fixtures.NOW.plusSeconds(1000), ZoneOffset.UTC), ZoneOffset.UTC);
        Reservation b = later.start("juan", 15L, Fixtures.MONDAY, LocalTime.of(9, 0));
        assertThat(b.id()).isNotEqualTo(a.id());
        assertThat(repo.findById(a.id()).get().status()).isEqualTo(ReservationStatus.EXPIRED);
    }

    @Test
    void horarioYaTomadoMarcaFailedYDevuelveConflicto() {
        HoldPort taken = (p, d, s, u) -> { throw new CatedraRejectedException("x", 409, "SLOT_ALREADY_HELD", null); };
        assertThatThrownBy(() -> service(taken, Fixtures.ANA).start("juan", 15L, Fixtures.MONDAY, LocalTime.of(9, 0)))
                .isInstanceOf(SlotUnavailableException.class);
        assertThat(repo.only().status()).isEqualTo(ReservationStatus.FAILED);
    }

    @Test
    void sinRespuestaDeLaCatedraQuedaPendienteParaQueLoRecupereElRecuperador() {
        HoldPort down = (p, d, s, u) -> { throw new CatedraUnavailableException("timeout", null); };
        assertThatThrownBy(() -> service(down, Fixtures.ANA).start("juan", 15L, Fixtures.MONDAY, LocalTime.of(9, 0)))
                .isInstanceOf(CatedraUnavailableException.class);
        assertThat(repo.only().status()).isEqualTo(ReservationStatus.HOLD_REQUESTED);
    }

    @Test
    void horarioQueNoExisteEnLaAgenda() {
        assertThatThrownBy(() -> service(okHold(), Fixtures.ANA).start("juan", 15L, Fixtures.MONDAY, LocalTime.of(9, 15)))
                .isInstanceOf(InvalidSlotException.class);
        assertThat(holdCalls).isEqualTo(0);
        assertThat(repo.all()).isEmpty();
    }

    @Test
    void horarioEnElPasado() {
        var late = new StartReservationService(id -> Fixtures.ANA, okHold(), repo,
                java.time.Clock.fixed(Instant.parse("2026-11-23T09:10:00Z"), ZoneOffset.UTC), ZoneOffset.UTC);
        assertThatThrownBy(() -> late.start("juan", 15L, Fixtures.MONDAY, LocalTime.of(9, 0)))
                .isInstanceOf(InvalidSlotException.class);
    }

    @Test
    void profesionalDeshabilitado() {
        var off = new ProfessionalSummary(15L, "Ana Gomez", "Clinica medica", false, Fixtures.ANA.schedules());
        assertThatThrownBy(() -> service(okHold(), off).start("juan", 15L, Fixtures.MONDAY, LocalTime.of(9, 0)))
                .isInstanceOf(ProfessionalDisabledException.class);
    }

    @Test
    void datosObligatorios() {
        assertThatThrownBy(() -> service(okHold(), Fixtures.ANA).start("juan", 15L, null, LocalTime.of(9, 0)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
