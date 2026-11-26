package com.integrador.turnos.domain.model;

import java.time.Instant;

/**
 * Converge el estado local al que informa la catedra (la catedra es la fuente de verdad del resultado).
 * Solo avanza por transiciones validas: un estado final local nunca se reabre.
 * Lo usan CU-14 (consultar reservas) y CU-16 (recuperacion).
 */
public final class ReservationReconciler {

    private ReservationReconciler() {
    }

    /** @return true si el estado local cambio. */
    public static boolean reconcile(Reservation local, AppointmentSnapshot remote, Instant now) {
        switch (remote.status()) {
            case CONFIRMED -> {
                if (local.canTransitionTo(ReservationStatus.CONFIRMED)) {
                    local.confirmed(remote.reservationId(), now);
                    return true;
                }
            }
            case CANCELLED -> {
                if (local.canTransitionTo(ReservationStatus.CANCELLED)) {
                    local.cancel(now);
                    return true;
                }
                if (local.isPending() && local.canTransitionTo(ReservationStatus.EXPIRED)) {
                    local.expire("CANCELLED_BY_CATEDRA", now);
                    return true;
                }
            }
            case FAILED -> {
                if (local.canTransitionTo(ReservationStatus.FAILED)) {
                    local.fail("CATEDRA_FAILED", now);
                    return true;
                }
            }
            case PHONE_PENDING -> {
                // la catedra esta esperando el telefono: si localmente seguimos en hold/confirmacion,
                // lo dejamos como esta (falta el requestEventId, que solo llega por Kafka)
            }
        }
        return false;
    }
}
