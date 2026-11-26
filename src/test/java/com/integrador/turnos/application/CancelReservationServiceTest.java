package com.integrador.turnos.application;

import com.integrador.turnos.application.exception.CatedraRejectedException;
import com.integrador.turnos.application.exception.CatedraUnavailableException;
import com.integrador.turnos.application.exception.ReservationNotCancellableException;
import com.integrador.turnos.application.exception.ReservationNotFoundException;
import com.integrador.turnos.application.port.out.AppointmentCancellationPort;
import com.integrador.turnos.application.service.CancelReservationService;
import com.integrador.turnos.domain.exception.InvalidReservationStateException;
import com.integrador.turnos.domain.model.Reservation;
import com.integrador.turnos.domain.model.ReservationStatus;
import com.integrador.turnos.support.FakeReservationRepository;
import com.integrador.turnos.support.Fixtures;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CancelReservationServiceTest {

    private final FakeReservationRepository repo = new FakeReservationRepository();
    private final List<String> cancelled = new ArrayList<>();
    private final AppointmentCancellationPort ok = (process, reason) -> {
        cancelled.add(process + "|" + reason);
        return new AppointmentCancellationPort.CancelResult(1L, "CANCELLED", Fixtures.NOW);
    };

    private String confirmed() {
        Reservation r = Fixtures.heldReservation("juan");
        r.confirmed(8251L, Fixtures.NOW);
        return repo.save(r).id();
    }

    private CancelReservationService service(AppointmentCancellationPort p) {
        return new CancelReservationService(repo, p, Fixtures.CLOCK);
    }

    @Test
    void cancelaUnaReservaPropiaConfirmada() {
        String id = confirmed();
        Reservation r = service(ok).cancel("juan", id, "Cambio de planes");
        assertThat(r.status()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(cancelled).containsExactly("process-1|Cambio de planes");
    }

    @Test
    void repetirLaCancelacionEsIdempotente() {
        String id = confirmed();
        var s = service(ok);
        s.cancel("juan", id, null);
        assertThat(s.cancel("juan", id, null).status()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(cancelled).hasSize(1);
    }

    @Test
    void reservaAjenaNoSeRevela() {
        String id = confirmed();
        assertThatThrownBy(() -> service(ok).cancel("otro", id, null)).isInstanceOf(ReservationNotFoundException.class);
        assertThat(cancelled).isEmpty();
    }

    @Test
    void unaReservaNoConfirmadaNoSePuedeCancelar() {
        String id = repo.save(Fixtures.heldReservation("juan")).id();
        assertThatThrownBy(() -> service(ok).cancel("juan", id, null)).isInstanceOf(InvalidReservationStateException.class);
        assertThat(cancelled).isEmpty();
    }

    @Test
    void siLaCatedraFallaElEstadoLocalNoCambia() {
        String id = confirmed();
        AppointmentCancellationPort down = (p, r) -> { throw new CatedraUnavailableException("caida", null); };
        assertThatThrownBy(() -> service(down).cancel("juan", id, null)).isInstanceOf(CatedraUnavailableException.class);
        assertThat(repo.findById(id).get().status()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void laCatedraDiceQueNoSePuedeCancelar() {
        String id = confirmed();
        AppointmentCancellationPort no = (p, r) -> { throw new CatedraRejectedException("x", 409, "RESERVATION_NOT_CANCELLABLE", null); };
        assertThatThrownBy(() -> service(no).cancel("juan", id, null)).isInstanceOf(ReservationNotCancellableException.class);
        assertThat(repo.findById(id).get().status()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void motivoDemasiadoLargo() {
        String id = confirmed();
        assertThatThrownBy(() -> service(ok).cancel("juan", id, "x".repeat(501))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void siElEventoDeCancelacionSeAdelantaNoFalla() {
        String id = confirmed();
        AppointmentCancellationPort racing = (p, r) -> {
            Reservation x = repo.findById(id).get();
            x.cancel(Fixtures.NOW);
            repo.save(x);
            return new AppointmentCancellationPort.CancelResult(1L, "CANCELLED", Fixtures.NOW);
        };
        assertThat(service(racing).cancel("juan", id, null).status()).isEqualTo(ReservationStatus.CANCELLED);
    }
}
