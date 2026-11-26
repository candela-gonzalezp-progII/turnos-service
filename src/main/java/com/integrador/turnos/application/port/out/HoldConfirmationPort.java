package com.integrador.turnos.application.port.out;

import java.time.Instant;

/** Puerto de salida: confirmacion inicial del hold por REST (la catedra luego pide el telefono por Kafka). */
public interface HoldConfirmationPort {

    /**
     * @throws com.integrador.turnos.application.exception.CatedraRejectedException    HOLD_EXPIRED, HOLD_NOT_FOUND, ...
     * @throws com.integrador.turnos.application.exception.CatedraUnavailableException si no se obtuvo respuesta
     */
    ConfirmResult confirmHold(String holdId, String externalPatientId, String firstName, String lastName);

    record ConfirmResult(String reservationProcessId, String status, Instant expiresAt) {
    }
}
