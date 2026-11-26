package com.integrador.turnos.infrastructure.kafka;

import com.integrador.turnos.domain.model.ReservationEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationEventParserTest {

    private final ReservationEventParser parser = new ReservationEventParser();

    @Test
    void pedidoDeTelefono() {
        String json = """
                {"eventId":"request-event-8a31","eventType":"AdditionalInformationRequested",
                 "reservationProcessId":"process-a92b47d0","holdId":"hold-6c0c2f41","requiredField":"PHONE_NUMBER",
                 "message":"Ingrese un numero","expiresAt":"2026-09-10T14:44:50Z","occurredAt":"2026-09-10T14:35:10Z",
                 "schemaVersion":1}""";
        var event = (ReservationEvent.InformationRequested) parser.parse(json).get();
        assertThat(event.eventId()).isEqualTo("request-event-8a31");
        assertThat(event.reservationProcessId()).isEqualTo("process-a92b47d0");
        assertThat(event.expiresAt()).isEqualTo(Instant.parse("2026-09-10T14:44:50Z"));
    }

    @Test
    void confirmacionConCamposQueNoUsamosYCamposNuevos() {
        String json = """
                {"eventId":"c1","eventType":"AppointmentConfirmed","groupId":"g","reservationProcessId":"p1",
                 "reservationId":8251,"professionalId":15,"date":"2026-09-14","startTime":"09:30","status":"CONFIRMED",
                 "campoFuturo":{"a":1},"schemaVersion":1}""";
        var event = (ReservationEvent.Confirmed) parser.parse(json).get();
        assertThat(event.reservationId()).isEqualTo(8251L);
    }

    @Test
    void rechazoVencimientoInvalidoYCancelacion() {
        assertThat(parser.parse("{\"eventId\":\"r\",\"eventType\":\"AdditionalInformationRejected\",\"reservationProcessId\":\"p\",\"reason\":\"INVALID_PHONE_NUMBER\",\"schemaVersion\":1}").get())
                .isInstanceOf(ReservationEvent.InformationRejected.class);
        assertThat(parser.parse("{\"eventId\":\"x\",\"eventType\":\"AppointmentProcessExpired\",\"reservationProcessId\":\"p\",\"schemaVersion\":1}").get())
                .isInstanceOf(ReservationEvent.ProcessExpired.class);
        assertThat(parser.parse("{\"eventId\":\"i\",\"eventType\":\"AppointmentProcessInvalid\",\"reservationProcessId\":\"p\",\"schemaVersion\":1}").get())
                .isInstanceOf(ReservationEvent.ProcessInvalid.class);
        assertThat(parser.parse("{\"eventId\":\"k\",\"eventType\":\"AppointmentCancelled\",\"reservationProcessId\":\"p\",\"reason\":null,\"schemaVersion\":1}").get())
                .isInstanceOf(ReservationEvent.Cancelled.class);
    }

    @Test
    void tipoDesconocidoOVersionFuturaSeIgnora() {
        assertThat(parser.parse("{\"eventId\":\"z\",\"eventType\":\"OtroEvento\",\"schemaVersion\":1}").isPresent()).isFalse();
        assertThat(parser.parse("{\"eventId\":\"z\",\"eventType\":\"AppointmentConfirmed\",\"reservationProcessId\":\"p\",\"schemaVersion\":2}").isPresent()).isFalse();
    }

    @Test
    void mensajeRotoOIncompletoEsMalformed() {
        assertThatThrownBy(() -> parser.parse("no es json")).isInstanceOf(MalformedEventException.class);
        assertThatThrownBy(() -> parser.parse("{\"eventType\":\"AppointmentConfirmed\",\"reservationProcessId\":\"p\"}"))
                .isInstanceOf(MalformedEventException.class);
        assertThatThrownBy(() -> parser.parse("{\"eventId\":\"a\",\"reservationProcessId\":\"p\"}"))
                .isInstanceOf(MalformedEventException.class);
    }
}
