package com.integrador.turnos.infrastructure.kafka;

import com.integrador.turnos.application.port.in.HandleReservationEventUseCase;
import com.integrador.turnos.domain.model.EventOutcome;
import com.integrador.turnos.domain.model.ReservationEvent;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Decorador (Unit of Work): el registro del eventId y el cambio de estado se confirman juntos o se deshacen juntos.
 * Asi un fallo a mitad de camino no deja un evento "procesado" sin efecto.
 */
public class TransactionalReservationEventHandler implements HandleReservationEventUseCase {

    private final HandleReservationEventUseCase delegate;
    private final TransactionTemplate tx;

    public TransactionalReservationEventHandler(HandleReservationEventUseCase delegate, TransactionTemplate tx) {
        this.delegate = delegate;
        this.tx = tx;
    }

    @Override
    public EventOutcome handle(ReservationEvent event) {
        return tx.execute(status -> delegate.handle(event));
    }
}
