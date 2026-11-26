package com.integrador.turnos.application.service;

import com.integrador.turnos.application.exception.ConcurrentReservationUpdateException;

import java.util.function.Supplier;

/** Repite una actualizacion "leer - modificar - guardar" si otro cambio se adelanto (maximo 3 intentos). */
final class OptimisticRetry {

    private static final int MAX_ATTEMPTS = 3;

    private OptimisticRetry() {
    }

    static <T> T run(Supplier<T> action) {
        for (int attempt = 1; ; attempt++) {
            try {
                return action.get();
            } catch (ConcurrentReservationUpdateException e) {
                if (attempt >= MAX_ATTEMPTS) {
                    throw e;
                }
            }
        }
    }
}
