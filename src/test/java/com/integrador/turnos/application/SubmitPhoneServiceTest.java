package com.integrador.turnos.application;

import com.integrador.turnos.application.exception.HoldExpiredException;
import com.integrador.turnos.application.exception.PhonePublishException;
import com.integrador.turnos.application.exception.ReservationNotFoundException;
import com.integrador.turnos.application.port.out.PhonePublisherPort;
import com.integrador.turnos.application.service.SubmitPhoneService;
import com.integrador.turnos.domain.exception.InvalidReservationStateException;
import com.integrador.turnos.domain.exception.ValidationException;
import com.integrador.turnos.domain.model.PatientName;
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

class SubmitPhoneServiceTest {

    private final FakeReservationRepository repo = new FakeReservationRepository();
    private final List<PhonePublisherPort.PhoneSubmission> published = new ArrayList<>();
    private final PhonePublisherPort ok = published::add;

    private String waitingForPhone() {
        Reservation r = Fixtures.heldReservation("juan");
        r.confirmationStarted(PatientName.of("Juan", "Perez"), Fixtures.NOW);
        r.infoRequested("request-evt-1", Fixtures.NOW.plusSeconds(900), Fixtures.NOW);
        return repo.save(r).id();
    }

    private SubmitPhoneService service(PhonePublisherPort p) {
        return new SubmitPhoneService(repo, p, Fixtures.CLOCK);
    }

    @Test
    void publicaElTelefonoConElRequestEventIdYMarcaEnviado() {
        String id = waitingForPhone();
        Reservation r = service(ok).submit("juan", id, "+54 261 555-1234");
        assertThat(r.status()).isEqualTo(ReservationStatus.INFO_SUBMITTED);
        assertThat(published).hasSize(1);
        assertThat(published.get(0).requestEventId()).isEqualTo("request-evt-1");
        assertThat(published.get(0).phoneNumber()).isEqualTo("+542615551234");
        assertThat(published.get(0).reservationProcessId()).isEqualTo("process-1");
        assertThat(published.get(0).holdId()).isEqualTo("hold-1");
    }

    @Test
    void sinPedidoDeTelefonoNoSePublicaNada() {
        String id = repo.save(Fixtures.heldReservation("juan")).id();   // todavia HOLD_CREATED
        assertThatThrownBy(() -> service(ok).submit("juan", id, "+542615551234"))
                .isInstanceOf(InvalidReservationStateException.class);
        assertThat(published).isEmpty();
    }

    @Test
    void telefonoInvalidoNoSePublica() {
        String id = waitingForPhone();
        assertThatThrownBy(() -> service(ok).submit("juan", id, "abc")).isInstanceOf(ValidationException.class);
        assertThat(published).isEmpty();
        assertThat(repo.findById(id).get().status()).isEqualTo(ReservationStatus.INFO_REQUESTED);
    }

    @Test
    void siKafkaFallaElEstadoNoCambiaYSePuedeReintentar() {
        String id = waitingForPhone();
        PhonePublisherPort down = s -> { throw new PhonePublishException("caido", null); };
        assertThatThrownBy(() -> service(down).submit("juan", id, "+542615551234")).isInstanceOf(PhonePublishException.class);
        assertThat(repo.findById(id).get().status()).isEqualTo(ReservationStatus.INFO_REQUESTED);
        assertThat(service(ok).submit("juan", id, "+542615551234").status()).isEqualTo(ReservationStatus.INFO_SUBMITTED);
    }

    @Test
    void despuesDeUnRechazoSePuedeReenviar() {
        String id = waitingForPhone();
        Reservation r = repo.findById(id).get();
        r.infoSubmitted("1234567", Fixtures.NOW);
        r.infoRejected("INVALID_PHONE_NUMBER", null, Fixtures.NOW);
        repo.save(r);
        assertThat(service(ok).submit("juan", id, "+542615551234").status()).isEqualTo(ReservationStatus.INFO_SUBMITTED);
    }

    @Test
    void procesoVencidoSeCierraYNoPublica() {
        String id = waitingForPhone();
        var late = new SubmitPhoneService(repo, ok, Clock.fixed(Fixtures.NOW.plusSeconds(2000), ZoneOffset.UTC));
        assertThatThrownBy(() -> late.submit("juan", id, "+542615551234")).isInstanceOf(HoldExpiredException.class);
        assertThat(repo.findById(id).get().status()).isEqualTo(ReservationStatus.EXPIRED);
        assertThat(published).isEmpty();
    }

    @Test
    void reservaAjena() {
        String id = waitingForPhone();
        assertThatThrownBy(() -> service(ok).submit("otro", id, "+542615551234")).isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    void siLaConfirmacionLlegaAntesDeGuardarNoRetrocede() {
        String id = waitingForPhone();
        PhonePublisherPort racing = s -> {   // mientras se publica, la catedra ya confirmo
            Reservation r = repo.findById(id).get();
            r.confirmed(77L, Fixtures.NOW);
            repo.save(r);
        };
        assertThat(service(racing).submit("juan", id, "+542615551234").status()).isEqualTo(ReservationStatus.CONFIRMED);
    }
}
