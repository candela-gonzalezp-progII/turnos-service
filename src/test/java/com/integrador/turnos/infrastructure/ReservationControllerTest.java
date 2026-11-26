package com.integrador.turnos.infrastructure;

import com.integrador.turnos.domain.model.Reservation;
import com.integrador.turnos.infrastructure.web.ReservationController;
import com.integrador.turnos.infrastructure.web.ReservationResponse;
import com.integrador.turnos.infrastructure.web.StartReservationRequest;
import com.integrador.turnos.support.Fixtures;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationControllerTest {

    private final Reservation held = Fixtures.heldReservation("juan");
    private final ReservationController controller = new ReservationController(
            (login, pid, date, start) -> held, (login, id, f, l) -> held, (login, id) -> held,
            (login, id, phone) -> held);

    @Test
    void iniciarDevuelve201ConElEstado() {
        var response = controller.start("juan", new StartReservationRequest(15L, Fixtures.MONDAY, LocalTime.of(9, 0)));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().status()).isEqualTo("HOLD_CREATED");
        assertThat(response.getBody().phoneRequired()).isFalse();
    }

    @Test
    void faltanDatosEs400() {
        assertThatThrownBy(() -> controller.start("juan", new StartReservationRequest(null, Fixtures.MONDAY, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void confirmarDevuelve202() {
        assertThat(controller.confirm("juan", held.id(), null).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    void laRespuestaNoExponeElUsuarioNiIdsDeLaCatedra() {
        ReservationResponse r = ReservationResponse.from(held);
        assertThat(r.toString().contains("hold-1")).isFalse();
        assertThat(r.toString().contains("process-1")).isFalse();
        assertThat(r.toString().contains("juan")).isFalse();
    }
}
