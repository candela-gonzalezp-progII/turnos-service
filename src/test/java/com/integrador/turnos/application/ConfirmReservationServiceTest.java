package com.integrador.turnos.application;

import com.integrador.turnos.application.exception.CatedraRejectedException;
import com.integrador.turnos.application.exception.HoldExpiredException;
import com.integrador.turnos.application.exception.ReservationNotFoundException;
import com.integrador.turnos.application.port.out.HoldConfirmationPort;
import com.integrador.turnos.application.service.ConfirmReservationService;
import com.integrador.turnos.application.service.GetReservationService;
import com.integrador.turnos.domain.exception.InvalidReservationStateException;
import com.integrador.turnos.domain.exception.ValidationException;
import com.integrador.turnos.domain.model.Reservation;
import com.integrador.turnos.domain.model.ReservationStatus;
import com.integrador.turnos.support.FakeReservationRepository;
import com.integrador.turnos.support.Fixtures;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfirmReservationServiceTest {

    private final FakeReservationRepository repo = new FakeReservationRepository();
    private final List<String> sent = new ArrayList<>();
    private final HoldConfirmationPort ok = (hold, patient, first, last) -> {
        sent.add(hold + "|" + patient + "|" + first + "|" + last);
        return new HoldConfirmationPort.ConfirmResult("process-1", "WAITING_FOR_PHONE", Fixtures.NOW.plusSeconds(900));
    };

    private ConfirmReservationService service(HoldConfirmationPort port) {
        return new ConfirmReservationService(repo, port, Fixtures.users(Fixtures.user("juan", "Juan", "Perez")),
                Fixtures.CLOCK);
    }

    private String held() {
        return repo.save(Fixtures.heldReservation("juan")).id();
    }

    @Test
    void confirmaConLosDatosRecibidos() {
        Reservation r = service(ok).confirm("juan", held(), "Maria", "Lopez");
        assertThat(r.status()).isEqualTo(ReservationStatus.CONFIRMATION_PENDING);
        assertThat(sent).containsExactly("hold-1|juan|Maria|Lopez");
    }

    @Test
    void siFaltaElNombreUsaElDeLaCuenta() {
        service(ok).confirm("juan", held(), null, null);
        assertThat(sent).containsExactly("hold-1|juan|Juan|Perez");
    }

    @Test
    void repetirLaConfirmacionNoVuelveAPegarleALaCatedra() {
        String id = held();
        var s = service(ok);
        s.confirm("juan", id, null, null);
        Reservation again = s.confirm("juan", id, null, null);
        assertThat(sent).hasSize(1);
        assertThat(again.status()).isEqualTo(ReservationStatus.CONFIRMATION_PENDING);
    }

    @Test
    void unUsuarioNoPuedeConfirmarLaReservaDeOtro() {
        String id = held();
        assertThatThrownBy(() -> service(ok).confirm("otro", id, null, null)).isInstanceOf(ReservationNotFoundException.class);
        assertThat(sent).isEmpty();
    }

    @Test
    void holdVencidoSeCierraSinLlamarALaCatedra() {
        String id = held();
        var late = new ConfirmReservationService(repo, ok, Fixtures.users(Fixtures.user("juan", "Juan", "Perez")),
                Clock.fixed(Fixtures.NOW.plusSeconds(1000), ZoneOffset.UTC));
        assertThatThrownBy(() -> late.confirm("juan", id, null, null)).isInstanceOf(HoldExpiredException.class);
        assertThat(repo.findById(id).get().status()).isEqualTo(ReservationStatus.EXPIRED);
        assertThat(sent).isEmpty();
    }

    @Test
    void laCatedraDiceQueElHoldVencio() {
        HoldConfirmationPort expired = (h, p, f, l) -> { throw new CatedraRejectedException("x", 410, "HOLD_EXPIRED", null); };
        String id = held();
        assertThatThrownBy(() -> service(expired).confirm("juan", id, null, null)).isInstanceOf(HoldExpiredException.class);
        assertThat(repo.findById(id).get().status()).isEqualTo(ReservationStatus.EXPIRED);
    }

    @Test
    void yaConfirmadaEnLaCatedraSeTrataComoExito() {
        HoldConfirmationPort already = (h, p, f, l) -> { throw new CatedraRejectedException("x", 409, "RESERVATION_PROCESS_ALREADY_CONFIRMED", null); };
        Reservation r = service(already).confirm("juan", held(), null, null);
        assertThat(r.status()).isEqualTo(ReservationStatus.CONFIRMATION_PENDING);
    }

    @Test
    void otroRechazoDeLaCatedraSePropaga() {
        HoldConfirmationPort bad = (h, p, f, l) -> { throw new CatedraRejectedException("x", 400, "VALIDATION_ERROR", null); };
        String id = held();
        assertThatThrownBy(() -> service(bad).confirm("juan", id, null, null)).isInstanceOf(CatedraRejectedException.class);
        assertThat(repo.findById(id).get().status()).isEqualTo(ReservationStatus.HOLD_CREATED);
    }

    @Test
    void nombreInvalido() {
        assertThatThrownBy(() -> service(ok).confirm("juan", held(), "J", "Lopez")).isInstanceOf(ValidationException.class);
        assertThat(sent).isEmpty();
    }

    @Test
    void reintentaSiOtroCambioSeAdelanta() {
        String id = held();
        repo.failNextSaves = 2;
        Reservation r = service(ok).confirm("juan", id, null, null);
        assertThat(r.status()).isEqualTo(ReservationStatus.CONFIRMATION_PENDING);
    }

    @Test
    void unaReservaFallidaNoSePuedeConfirmar() {
        Reservation failed = Fixtures.heldReservation("juan");
        failed.fail("X", Fixtures.NOW);
        String id = repo.save(failed).id();
        assertThatThrownBy(() -> service(ok).confirm("juan", id, null, null)).isInstanceOf(InvalidReservationStateException.class);
    }

    @Test
    void consultarUnaReservaVencidaLaCierra() {
        String id = held();
        var get = new GetReservationService(repo, Clock.fixed(Fixtures.NOW.plusSeconds(1000), ZoneOffset.UTC));
        assertThat(get.get("juan", id).status()).isEqualTo(ReservationStatus.EXPIRED);
        assertThatThrownBy(() -> get.get("otro", id)).isInstanceOf(ReservationNotFoundException.class);
    }
}
