package com.integrador.turnos.application.port.out;

import java.time.Instant;

/** Puerto de salida: publicar AdditionalInformationSubmitted en catedra.turnos.telefono.{groupId}. */
public interface PhonePublisherPort {

    /** @throws com.integrador.turnos.application.exception.PhonePublishException si Kafka no confirma el envio */
    void publish(PhoneSubmission submission);

    record PhoneSubmission(String eventId, String reservationProcessId, String holdId, String requestEventId,
                           String phoneNumber, Instant occurredAt) {

        /** El telefono no sale en logs. */
        @Override
        public String toString() {
            return "PhoneSubmission[eventId=" + eventId + ", process=" + reservationProcessId + "]";
        }
    }
}
