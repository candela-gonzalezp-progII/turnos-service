package com.integrador.turnos.application;

import com.integrador.turnos.application.exception.CatedraUnavailableException;
import com.integrador.turnos.application.port.in.RecoverPendingReservationsUseCase.RecoveryReport;
import com.integrador.turnos.application.port.out.AppointmentQueryPort;
import com.integrador.turnos.application.service.RecoveryService;
import com.integrador.turnos.domain.model.AppointmentSnapshot;
import com.integrador.turnos.domain.model.AppointmentStatus;
import com.integrador.turnos.domain.model.PatientName;
import com.integrador.turnos.domain.model.Reservation;
import com.integrador.turnos.domain.model.ReservationStatus;
import com.integrador.turnos.support.FakeReservationRepository;
import com.integrador.turnos.support.Fixtures;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class RecoveryServiceTest {

    private final FakeReservationRepository repo = new FakeReservationRepository();

    private RecoveryService service(AppointmentQueryPort port, long secondsAfterNow) {
        return new RecoveryService(repo, port, Clock.fixed(Fixtures.NOW.plusSeconds(secondsAfterNow), ZoneOffset.UTC),
                Duration.ofMinutes(2), Duration.ofMinutes(10));
    }

    private String confirmationPending(String user, String process) {
        Reservation r = Fixtures.heldReservation(user, "hold-" + process, process);
        r.confirmationStarted(PatientName.of("Juan", "Perez"), Fixtures.NOW);
        return repo.save(r).id();
    }

    private String holdRequested(String user) {
        return repo.save(Reservation.request(user, 15L, "Ana", Fixtures.MONDAY, LocalTime.of(9, 0), LocalTime.of(9, 30), Fixtures.NOW)).id();
    }

    private static AppointmentSnapshot snap(String process, AppointmentStatus s) {
        return new AppointmentSnapshot(1L, process, "juan", Fixtures.MONDAY, LocalTime.of(9, 0), s);
    }

    @Test
    void sinPendientesNoHaceNada() {
        RecoveryReport r = service(p -> { throw new AssertionError(); }, 0).recover();
        assertThat(r.examined()).isEqualTo(0);
    }

    @Test
    void reinicioConUnProcesoQueLaCatedraYaConfirmoLoConverge() {
        String id = confirmationPending("juan", "p1");
        RecoveryReport r = service(p -> List.of(snap("p1", AppointmentStatus.CONFIRMED)), 60).recover();
        assertThat(r.updated()).isEqualTo(1);
        assertThat(repo.findById(id).get().status()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void procesoVencidoSeCierraDespuesDeConsultarALaCatedra() {
        String id = confirmationPending("juan", "p1");
        service(p -> List.of(), 1000).recover();
        assertThat(repo.findById(id).get().status()).isEqualTo(ReservationStatus.EXPIRED);
    }

    @Test
    void procesoVigenteQuedaPendiente() {
        String id = confirmationPending("juan", "p1");
        RecoveryReport r = service(p -> List.of(snap("p1", AppointmentStatus.PHONE_PENDING)), 60).recover();
        assertThat(r.updated()).isEqualTo(0);
        assertThat(repo.findById(id).get().status()).isEqualTo(ReservationStatus.CONFIRMATION_PENDING);
    }

    @Test
    void conLaCatedraCaidaNoCierraUnVencidoReciente() {
        String id = confirmationPending("juan", "p1");
        AppointmentQueryPort down = p -> { throw new CatedraUnavailableException("caida", null); };
        RecoveryReport r = service(down, 1000).recover();      // vencio hace ~100 s, margen de 10 min
        assertThat(r.catedraReachable()).isFalse();
        assertThat(repo.findById(id).get().status()).isEqualTo(ReservationStatus.CONFIRMATION_PENDING);
    }

    @Test
    void conLaCatedraCaidaCierraUnVencidoMuyViejo() {
        String id = confirmationPending("juan", "p1");
        AppointmentQueryPort down = p -> { throw new CatedraUnavailableException("caida", null); };
        service(down, 900 + 700).recover();
        assertThat(repo.findById(id).get().status()).isEqualTo(ReservationStatus.EXPIRED);
    }

    @Test
    void holdSinRespuestaSeCierraPasadoElTimeoutPeroNoAntes() {
        String id = holdRequested("juan");
        service(p -> List.of(), 60).recover();
        assertThat(repo.findById(id).get().status()).isEqualTo(ReservationStatus.HOLD_REQUESTED);
        service(p -> List.of(), 130).recover();
        assertThat(repo.findById(id).get().status()).isEqualTo(ReservationStatus.FAILED);
        assertThat(repo.findById(id).get().reason()).isEqualTo("HOLD_NOT_CONFIRMED");
    }

    @Test
    void consultaUnaSolaVezPorUsuarioYNoInsisteSiFalla() {
        confirmationPending("juan", "p1");
        confirmationPending("ana", "p2");
        confirmationPending("juan", "p3");
        AtomicInteger calls = new AtomicInteger();
        AppointmentQueryPort down = p -> { calls.incrementAndGet(); throw new CatedraUnavailableException("caida", null); };
        // las 3 reservas son del mismo horario: se usan distintos usuarios/horarios solo por los ids; lo que importa es el conteo
        service(down, 60).recover();
        assertThat(calls.get()).isEqualTo(1);       // al primer fallo no se insiste en la pasada
    }

    @Test
    void unaVezCerradoNoSeVuelveAProcesar() {
        confirmationPending("juan", "p1");
        var s = service(p -> List.of(), 1000);
        s.recover();
        assertThat(s.recover().examined()).isEqualTo(0);
    }
}
