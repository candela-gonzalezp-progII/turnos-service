package com.integrador.turnos.infrastructure.catedra;

import com.integrador.turnos.application.exception.CatedraRejectedException;
import com.integrador.turnos.application.exception.CatedraUnavailableException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryExecutorTest {

    private final RetryExecutor retry = new RetryExecutor(3, Duration.ZERO);

    @Test
    void reintentaErroresTransitoriosYTerminaOk() {
        AtomicInteger calls = new AtomicInteger();
        String r = retry.execute(() -> {
            if (calls.incrementAndGet() < 3) {
                throw new CatedraUnavailableException("caida", null);
            }
            return "ok";
        });
        assertThat(r).isEqualTo("ok");
        assertThat(calls.get()).isEqualTo(3);
    }

    @Test
    void sePlantaAlLlegarAlMaximo() {
        AtomicInteger calls = new AtomicInteger();
        assertThatThrownBy(() -> retry.execute(() -> {
            calls.incrementAndGet();
            throw new CatedraUnavailableException("caida", null);
        })).isInstanceOf(CatedraUnavailableException.class);
        assertThat(calls.get()).isEqualTo(3);
    }

    @Test
    void unRechazoDeNegocioNoSeReintenta() {
        AtomicInteger calls = new AtomicInteger();
        assertThatThrownBy(() -> retry.execute(() -> {
            calls.incrementAndGet();
            throw new CatedraRejectedException("no", 409, "SLOT_ALREADY_HELD", null);
        })).isInstanceOf(CatedraRejectedException.class);
        assertThat(calls.get()).isEqualTo(1);
    }
}
