package com.integrador.turnos.application;

import com.integrador.turnos.application.service.ReservationEventService;
import com.integrador.turnos.domain.model.EventOutcome;
import com.integrador.turnos.domain.model.PatientName;
import com.integrador.turnos.domain.model.Reservation;
import com.integrador.turnos.domain.model.ReservationEvent;
import com.integrador.turnos.domain.model.ReservationStatus;
import com.integrador.turnos.support.FakeProcessedEvents;
import com.integrador.turnos.support.FakeReservationRepository;
import com.integrador.turnos.support.Fixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationEventServiceTest {

    private final FakeReservationRepository repo = new FakeReservationRepository();
    private final FakeProcessedEvents processed = new FakeProcessedEvents();
    private final ReservationEventService service = new ReservationEventService(repo, processed, Fixtures.CLOCK);

    private String pending() {
        Reservation r = Fixtures.heldReservation("juan");
        r.confirmationStarted(PatientName.of("Juan", "Perez"), Fixtures.NOW);
        return repo.save(r).id();
    }

    private ReservationStatus status(String id) {
        return repo.findById(id).get().status();
    }

    @Test
    void pedidoDeTelefonoGuardaElRequestEventIdYElVencimiento() {
        String id = pending();
        var exp = Fixtures.NOW.plusSeconds(600);
        assertThat(service.handle(new ReservationEvent.InformationRequested("e1", "process-1", "hold-1", exp)))
                .isEqualTo(EventOutcome.APPLIED);
        Reservation r = repo.findById(id).get();
        assertThat(r.status()).isEqualTo(ReservationStatus.INFO_REQUESTED);
        assertThat(r.requestEventId()).isEqualTo("e1");
        assertThat(r.holdExpiresAt()).isEqualTo(exp);
        assertThat(r.requiresPhone()).isTrue();
    }

    @Test
    void elMismoEventoDosVecesSeAplicaUnaSolaVez() {
        String id = pending();
        var evt = new ReservationEvent.Confirmed("e2", "process-1", 8251L);
        assertThat(service.handle(evt)).isEqualTo(EventOutcome.APPLIED);
        int savesAfterFirst = repo.saves;
        assertThat(service.handle(evt)).isEqualTo(EventOutcome.DUPLICATE);
        assertThat(repo.saves).isEqualTo(savesAfterFirst);
        assertThat(repo.findById(id).get().catedraReservationId()).isEqualTo(8251L);
    }

    @Test
    void confirmacionLlevaALaReservaAConfirmed() {
        String id = pending();
        service.handle(new ReservationEvent.Confirmed("e3", "process-1", 5L));
        assertThat(status(id)).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void rechazoDeTelefonoPermiteReintentar() {
        String id = pending();
        service.handle(new ReservationEvent.InformationRequested("e4", "process-1", "hold-1", null));
        service.handle(new ReservationEvent.InformationRejected("e5", "process-1", "INVALID_PHONE_NUMBER", Fixtures.NOW.plusSeconds(500)));
        assertThat(status(id)).isEqualTo(ReservationStatus.INFO_REJECTED);
        assertThat(repo.findById(id).get().requiresPhone()).isTrue();
    }

    @Test
    void vencidaEInvalidaSonFinales() {
        String a = pending();
        service.handle(new ReservationEvent.ProcessExpired("e6", "process-1", "HOLD_EXPIRED"));
        assertThat(status(a)).isEqualTo(ReservationStatus.EXPIRED);
        assertThat(repo.findById(a).get().reason()).isEqualTo("HOLD_EXPIRED");
    }

    @Test
    void procesoInvalido() {
        String a = pending();
        service.handle(new ReservationEvent.ProcessInvalid("e7", "process-1", "REQUEST_EVENT_MISMATCH"));
        assertThat(status(a)).isEqualTo(ReservationStatus.INVALID);
    }

    @Test
    void unEventoTardioNoReabreUnEstadoFinal() {
        String id = pending();
        service.handle(new ReservationEvent.ProcessExpired("e8", "process-1", "HOLD_EXPIRED"));
        assertThat(service.handle(new ReservationEvent.Confirmed("e9", "process-1", 1L))).isEqualTo(EventOutcome.IGNORED);
        assertThat(service.handle(new ReservationEvent.InformationRequested("e10", "process-1", "h", null))).isEqualTo(EventOutcome.IGNORED);
        assertThat(status(id)).isEqualTo(ReservationStatus.EXPIRED);
    }

    @Test
    void cancelacionSoloSobreConfirmada() {
        String id = pending();
        assertThat(service.handle(new ReservationEvent.Cancelled("e11", "process-1"))).isEqualTo(EventOutcome.IGNORED);
        service.handle(new ReservationEvent.Confirmed("e12", "process-1", 1L));
        assertThat(service.handle(new ReservationEvent.Cancelled("e13", "process-1"))).isEqualTo(EventOutcome.APPLIED);
        assertThat(status(id)).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void procesoDesconocidoNoRegistraElEvento() {
        pending();
        assertThat(service.handle(new ReservationEvent.Confirmed("e14", "process-999", 1L))).isEqualTo(EventOutcome.UNKNOWN_PROCESS);
        assertThat(processed.seen.contains("e14")).isFalse();   // podra aplicarse cuando el proceso exista
    }

    @Test
    void unEventoIgnoradoQuedaRegistradoYNoVuelveAProcesarse() {
        pending();
        service.handle(new ReservationEvent.ProcessExpired("e15", "process-1", null));
        var late = new ReservationEvent.Confirmed("e16", "process-1", 1L);
        assertThat(service.handle(late)).isEqualTo(EventOutcome.IGNORED);
        assertThat(service.handle(late)).isEqualTo(EventOutcome.DUPLICATE);
    }
}
