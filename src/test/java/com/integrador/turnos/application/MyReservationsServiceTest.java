package com.integrador.turnos.application;

import com.integrador.turnos.application.exception.CatedraUnavailableException;
import com.integrador.turnos.application.port.in.MyReservationsUseCase;
import com.integrador.turnos.application.port.out.AppointmentQueryPort;
import com.integrador.turnos.application.service.MyReservationsService;
import com.integrador.turnos.domain.model.AppointmentSnapshot;
import com.integrador.turnos.domain.model.AppointmentStatus;
import com.integrador.turnos.domain.model.PatientName;
import com.integrador.turnos.domain.model.Reservation;
import com.integrador.turnos.domain.model.ReservationStatus;
import com.integrador.turnos.support.FakeReservationRepository;
import com.integrador.turnos.support.Fixtures;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MyReservationsServiceTest {

    private final FakeReservationRepository repo = new FakeReservationRepository();

    private String pendingOf(String user, String processId) {
        Reservation r = Fixtures.heldReservation(user, "hold-" + processId, processId);
        r.confirmationStarted(PatientName.of("Juan", "Perez"), Fixtures.NOW);
        return repo.save(r).id();
    }

    private MyReservationsService service(AppointmentQueryPort port) {
        return new MyReservationsService(repo, port, Fixtures.CLOCK);
    }

    @Test
    void sinReservasDevuelveListaVaciaSinLlamarALaCatedra() {
        var r = service(p -> { throw new AssertionError("no debia llamarse"); }).list("juan");
        assertThat(r.reservations()).isEmpty();
        assertThat(r.verified()).isTrue();
    }

    @Test
    void unUsuarioNoVeLasReservasDeOtro() {
        pendingOf("juan", "p-juan");
        pendingOf("ana", "p-ana");
        var r = service(p -> List.of()).list("juan");
        assertThat(r.reservations()).hasSize(1);
        assertThat(r.reservations().get(0).userLogin()).isEqualTo("juan");
    }

    @Test
    void contrastaConLaCatedraYConvergeElEstado() {
        String id = pendingOf("juan", "p1");
        var remote = List.of(new AppointmentSnapshot(55L, "p1", "juan", Fixtures.MONDAY, LocalTime.of(9, 0), AppointmentStatus.CONFIRMED));
        var r = service(p -> remote).list("juan");
        assertThat(r.verified()).isTrue();
        assertThat(r.reservations().get(0).status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(repo.findById(id).get().status()).isEqualTo(ReservationStatus.CONFIRMED);   // quedo guardado
    }

    @Test
    void conLaCatedraCaidaMuestraLoLocalNoVerificado() {
        pendingOf("juan", "p1");
        var r = service(p -> { throw new CatedraUnavailableException("caida", null); }).list("juan");
        assertThat(r.verified()).isFalse();
        assertThat(r.reservations()).hasSize(1);
        assertThat(r.reservations().get(0).status()).isEqualTo(ReservationStatus.CONFIRMATION_PENDING);
    }

    @Test
    void cierraLasPendientesVencidas() {
        pendingOf("juan", "p1");
        var late = new MyReservationsService(repo, p -> List.of(), Clock.fixed(Fixtures.NOW.plusSeconds(5000), ZoneOffset.UTC));
        MyReservationsUseCase.MyReservations r = late.list("juan");
        assertThat(r.reservations().get(0).status()).isEqualTo(ReservationStatus.EXPIRED);
    }

    @Test
    void siOtroProcesoLaActualizaALaVezSeMuestraLoMasNuevo() {
        pendingOf("juan", "p1");
        repo.failNextSaves = 1;
        var remote = List.of(new AppointmentSnapshot(55L, "p1", "juan", Fixtures.MONDAY, LocalTime.of(9, 0), AppointmentStatus.CONFIRMED));
        var r = service(p -> remote).list("juan");
        assertThat(r.reservations()).hasSize(1);   // no explota
    }
}
