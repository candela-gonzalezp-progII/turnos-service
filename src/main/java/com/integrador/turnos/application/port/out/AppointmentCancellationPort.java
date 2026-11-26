package com.integrador.turnos.application.port.out;

import java.time.Instant;

/** Puerto de salida: cancelar una reserva confirmada en la catedra (idempotente en la catedra). */
public interface AppointmentCancellationPort {

    CancelResult cancel(String reservationProcessId, String reason);

    record CancelResult(Long reservationId, String status, Instant cancelledAt) {
    }
}
