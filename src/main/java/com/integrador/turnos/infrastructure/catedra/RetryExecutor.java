package com.integrador.turnos.infrastructure.catedra;

import com.integrador.turnos.application.exception.CatedraUnavailableException;

import java.time.Duration;
import java.util.function.Supplier;

/** Reintentos acotados con backoff exponencial. Solo para operaciones de lectura (GET). */
public class RetryExecutor {

    private final int maxAttempts;
    private final Duration backoff;

    public RetryExecutor(int maxAttempts, Duration backoff) {
        this.maxAttempts = Math.max(1, maxAttempts);
        this.backoff = backoff;
    }

    public <T> T execute(Supplier<T> action) {
        long wait = backoff.toMillis();
        for (int attempt = 1; ; attempt++) {
            try {
                return action.get();
            } catch (CatedraUnavailableException e) {
                if (attempt >= maxAttempts) {
                    throw e;
                }
                sleep(wait);
                wait *= 2;
            }
        }
    }

    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CatedraUnavailableException("Interrumpido durante el reintento", e);
        }
    }
}
