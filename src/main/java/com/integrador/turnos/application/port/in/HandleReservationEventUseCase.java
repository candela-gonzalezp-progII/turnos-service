package com.integrador.turnos.application.port.in;

import com.integrador.turnos.domain.model.EventOutcome;
import com.integrador.turnos.domain.model.ReservationEvent;

/** CU-13: aplicar un evento de la catedra al proceso local (idempotente por eventId). */
public interface HandleReservationEventUseCase {

    EventOutcome handle(ReservationEvent event);
}
