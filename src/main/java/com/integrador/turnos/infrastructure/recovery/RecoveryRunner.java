package com.integrador.turnos.infrastructure.recovery;

import com.integrador.turnos.application.port.in.RecoverPendingReservationsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;

/** Dispara la recuperacion al arrancar y despues periodicamente. Un fallo se registra y no tumba la app. */
public class RecoveryRunner {

    private static final Logger log = LoggerFactory.getLogger(RecoveryRunner.class);

    private final RecoverPendingReservationsUseCase recovery;

    public RecoveryRunner(RecoverPendingReservationsUseCase recovery) {
        this.recovery = recovery;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        run("arranque");
    }

    @Scheduled(initialDelayString = "${turnos.recovery-interval:60s}", fixedDelayString = "${turnos.recovery-interval:60s}")
    public void periodic() {
        run("periodica");
    }

    void run(String origin) {
        try {
            var report = recovery.recover();
            if (report.examined() > 0) {
                log.info("Recuperacion {}: {} procesos pendientes, {} actualizados, catedra {}", origin,
                        report.examined(), report.updated(), report.catedraReachable() ? "ok" : "no disponible");
            }
        } catch (RuntimeException e) {
            log.warn("Recuperacion {} fallida ({}); se reintenta en la proxima pasada", origin, e.getClass().getSimpleName());
        }
    }
}
