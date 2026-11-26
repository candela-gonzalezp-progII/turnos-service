package com.integrador.turnos.domain;

import com.integrador.turnos.domain.model.AppointmentSnapshot;
import com.integrador.turnos.domain.model.AppointmentStatus;
import com.integrador.turnos.domain.model.PatientName;
import com.integrador.turnos.domain.model.Reservation;
import com.integrador.turnos.domain.model.ReservationReconciler;
import com.integrador.turnos.domain.model.ReservationStatus;
import com.integrador.turnos.support.Fixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationReconcilerTest {

    private static AppointmentSnapshot remote(AppointmentStatus s) {
        return new AppointmentSnapshot(8251L, "process-1", "juan", Fixtures.MONDAY, java.time.LocalTime.of(9, 0), s);
    }

    private Reservation waiting() {
        Reservation r = Fixtures.heldReservation("juan");
        r.confirmationStarted(PatientName.of("Juan", "Perez"), Fixtures.NOW);
        return r;
    }

    @Test
    void confirmadaEnLaCatedraConfirmaLaLocal() {
        Reservation r = waiting();
        assertThat(ReservationReconciler.reconcile(r, remote(AppointmentStatus.CONFIRMED), Fixtures.NOW)).isTrue();
        assertThat(r.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(r.catedraReservationId()).isEqualTo(8251L);
    }

    @Test
    void canceladaEnLaCatedraCancelaLaConfirmada() {
        Reservation r = waiting();
        r.confirmed(8251L, Fixtures.NOW);
        assertThat(ReservationReconciler.reconcile(r, remote(AppointmentStatus.CANCELLED), Fixtures.NOW)).isTrue();
        assertThat(r.status()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void fallidaEnLaCatedraFallaLaPendiente() {
        Reservation r = waiting();
        ReservationReconciler.reconcile(r, remote(AppointmentStatus.FAILED), Fixtures.NOW);
        assertThat(r.status()).isEqualTo(ReservationStatus.FAILED);
    }

    @Test
    void phonePendingNoCambiaNada() {
        Reservation r = waiting();
        assertThat(ReservationReconciler.reconcile(r, remote(AppointmentStatus.PHONE_PENDING), Fixtures.NOW)).isFalse();
        assertThat(r.status()).isEqualTo(ReservationStatus.CONFIRMATION_PENDING);
    }

    @Test
    void nuncaReabreUnEstadoFinal() {
        Reservation r = waiting();
        r.expire("HOLD_EXPIRED", Fixtures.NOW);
        assertThat(ReservationReconciler.reconcile(r, remote(AppointmentStatus.CONFIRMED), Fixtures.NOW)).isFalse();
        assertThat(r.status()).isEqualTo(ReservationStatus.EXPIRED);
    }

    @Test
    void yaConfirmadaNoSeRepite() {
        Reservation r = waiting();
        r.confirmed(1L, Fixtures.NOW);
        assertThat(ReservationReconciler.reconcile(r, remote(AppointmentStatus.CONFIRMED), Fixtures.NOW)).isFalse();
    }
}
