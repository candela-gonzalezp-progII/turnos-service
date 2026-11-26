package com.integrador.turnos.infrastructure.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.integrador.turnos.domain.model.ReservationEvent;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Optional;

/**
 * JSON -> evento de dominio. Ignora campos desconocidos (contrato v1: pueden agregarse campos opcionales) y
 * devuelve vacio para tipos de evento que no usamos. Un JSON roto o sin campos obligatorios lanza
 * {@link MalformedEventException}.
 */
public class ReservationEventParser {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Raw(String eventId, String eventType, String reservationProcessId, String holdId, String reason,
               String expiresAt, Long reservationId, Integer schemaVersion) {
    }

    private final JsonMapper mapper = JsonMapper.builder().build();

    public Optional<ReservationEvent> parse(String json) {
        Raw raw;
        try {
            raw = mapper.readValue(json, Raw.class);
        } catch (RuntimeException e) {
            throw new MalformedEventException("JSON invalido", e);
        }
        if (raw == null || isBlank(raw.eventType())) {
            throw new MalformedEventException("Falta eventType", null);
        }
        if (raw.schemaVersion() != null && raw.schemaVersion() != 1) {
            return Optional.empty();                    // version futura: no la entendemos, se ignora
        }
        return switch (raw.eventType()) {
            case "AdditionalInformationRequested" -> Optional.of(new ReservationEvent.InformationRequested(
                    required(raw.eventId(), "eventId"), required(raw.reservationProcessId(), "reservationProcessId"),
                    raw.holdId(), instant(raw.expiresAt())));
            case "AppointmentConfirmed" -> Optional.of(new ReservationEvent.Confirmed(
                    required(raw.eventId(), "eventId"), required(raw.reservationProcessId(), "reservationProcessId"),
                    raw.reservationId()));
            case "AdditionalInformationRejected" -> Optional.of(new ReservationEvent.InformationRejected(
                    required(raw.eventId(), "eventId"), required(raw.reservationProcessId(), "reservationProcessId"),
                    raw.reason(), instant(raw.expiresAt())));
            case "AppointmentProcessExpired" -> Optional.of(new ReservationEvent.ProcessExpired(
                    required(raw.eventId(), "eventId"), required(raw.reservationProcessId(), "reservationProcessId"),
                    raw.reason()));
            case "AppointmentProcessInvalid" -> Optional.of(new ReservationEvent.ProcessInvalid(
                    required(raw.eventId(), "eventId"), required(raw.reservationProcessId(), "reservationProcessId"),
                    raw.reason()));
            case "AppointmentCancelled" -> Optional.of(new ReservationEvent.Cancelled(
                    required(raw.eventId(), "eventId"), required(raw.reservationProcessId(), "reservationProcessId")));
            default -> Optional.empty();
        };
    }

    private static String required(String value, String name) {
        if (isBlank(value)) {
            throw new MalformedEventException("Falta " + name, null);
        }
        return value;
    }

    private static Instant instant(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (RuntimeException e) {
            throw new MalformedEventException("expiresAt invalido", e);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
