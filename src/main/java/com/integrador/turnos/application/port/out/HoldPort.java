package com.integrador.turnos.application.port.out;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/** Puerto de salida: crear el bloqueo temporal (hold) en la catedra. */
public interface HoldPort {

    /**
     * @throws com.integrador.turnos.application.exception.CatedraRejectedException    SLOT_ALREADY_HELD, INVALID_SLOT, ...
     * @throws com.integrador.turnos.application.exception.CatedraUnavailableException si no se obtuvo respuesta
     */
    HoldResult createHold(long professionalId, LocalDate date, LocalTime startTime, String externalPatientId);

    record HoldResult(String holdId, String reservationProcessId, Instant expiresAt) {
    }
}
