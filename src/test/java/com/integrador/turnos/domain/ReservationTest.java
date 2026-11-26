package com.integrador.turnos.domain;

import com.integrador.turnos.domain.exception.InvalidReservationStateException;
import com.integrador.turnos.domain.model.PatientName;
import com.integrador.turnos.domain.model.Reservation;
import com.integrador.turnos.domain.model.ReservationStatus;
import com.integrador.turnos.domain.exception.ValidationException;
import com.integrador.turnos.support.Fixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    private static final Instant NOW = Fixtures.NOW;

    @Test
    void caminoFelizHastaConfirmada() {
        Reservation r = Fixtures.heldReservation("juan");
        r.confirmationStarted(PatientName.of("Juan", "Perez"), NOW);
        r.infoRequested("evt-1", NOW.plusSeconds(600), NOW);
        r.infoSubmitted("+5492615551234", NOW);
        r.confirmed(8251L, NOW);
        assertThat(r.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(r.catedraReservationId()).isEqualTo(8251L);
        assertThat(r.isFinal()).isTrue();
    }

    @Test
    void rechazoDeTelefonoPermiteReenviar() {
        Reservation r = Fixtures.heldReservation("juan");
        r.confirmationStarted(PatientName.of("Juan", "Perez"), NOW);
        r.infoRequested("evt-1", null, NOW);
        r.infoSubmitted("123", NOW);
        r.infoRejected("PHONE_INVALID", null, NOW);
        assertThat(r.requiresPhone()).isTrue();
        r.infoSubmitted("+5492615551234", NOW);
        assertThat(r.status()).isEqualTo(ReservationStatus.INFO_SUBMITTED);
    }

    @Test
    void unEstadoFinalNoRetrocede() {
        Reservation r = Fixtures.heldReservation("juan");
        r.expire("HOLD_EXPIRED", NOW);
        assertThatThrownBy(() -> r.confirmed(1L, NOW)).isInstanceOf(InvalidReservationStateException.class);
        assertThatThrownBy(() -> r.infoRequested("e", null, NOW)).isInstanceOf(InvalidReservationStateException.class);
        assertThatThrownBy(() -> r.cancel(NOW)).isInstanceOf(InvalidReservationStateException.class);
    }

    @Test
    void soloUnaReservaConfirmadaSePuedeCancelar() {
        Reservation pending = Fixtures.heldReservation("juan");
        assertThatThrownBy(() -> pending.cancel(NOW)).isInstanceOf(InvalidReservationStateException.class);
        pending.confirmed(1L, NOW);
        pending.cancel(NOW);
        assertThat(pending.status()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void laConfirmacionLlegaPrimeroQueElPedidoDeTelefono() {
        Reservation r = Fixtures.heldReservation("juan");
        r.confirmed(5L, NOW);   // evento fuera de orden: de HOLD_CREATED directo a CONFIRMED
        assertThat(r.status()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void vencimientoSegunLaCatedra() {
        Reservation r = Fixtures.heldReservation("juan");
        assertThat(r.isOverdueAt(NOW)).isFalse();
        assertThat(r.isOverdueAt(NOW.plusSeconds(901))).isTrue();
        assertThat(r.isHoldValidAt(NOW.plusSeconds(899))).isTrue();
        r.confirmed(1L, NOW);
        assertThat(r.isOverdueAt(NOW.plusSeconds(9999))).isFalse();   // una confirmada no vence
    }

    @Test
    void validaNombreDelPaciente() {
        assertThatThrownBy(() -> PatientName.of("J", "Perez")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> PatientName.of("Juan", " ")).isInstanceOf(ValidationException.class);
        assertThat(PatientName.of(" Juan ", "Perez").firstName()).isEqualTo("Juan");
    }

    @Test
    void toStringNoExponeDatosPersonales() {
        Reservation r = Fixtures.heldReservation("juan");
        r.confirmationStarted(PatientName.of("Juan", "Perez"), NOW);
        r.infoRequested("e", null, NOW);
        r.infoSubmitted("+5492615551234", NOW);
        assertThat(r.toString().contains("5492615551234")).isFalse();
        assertThat(r.toString().contains("Perez")).isFalse();
    }

    @Test
    void conElTelefonoEnviadoSeEsperaUnMargenAntesDeVencer() {
        Reservation r = Fixtures.heldReservation("juan");
        r.confirmationStarted(PatientName.of("Juan", "Perez"), NOW);
        r.infoRequested("e", null, NOW);
        r.infoSubmitted("+5492615551234", NOW);
        assertThat(r.isOverdueAt(NOW.plusSeconds(901))).isFalse();     // paso el vencimiento pero esta en el margen
        assertThat(r.isOverdueAt(NOW.plusSeconds(900 + 121))).isTrue();
    }
}
