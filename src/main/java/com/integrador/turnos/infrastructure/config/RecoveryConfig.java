package com.integrador.turnos.infrastructure.config;

import com.integrador.turnos.application.port.in.RecoverPendingReservationsUseCase;
import com.integrador.turnos.application.port.out.AppointmentQueryPort;
import com.integrador.turnos.application.port.out.ReservationRepositoryPort;
import com.integrador.turnos.application.service.RecoveryService;
import com.integrador.turnos.infrastructure.recovery.RecoveryRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.time.Duration;

@Configuration
@EnableScheduling
public class RecoveryConfig {

    @Bean
    RecoverPendingReservationsUseCase recoverPendingReservationsUseCase(ReservationRepositoryPort reservations,
                                                                         AppointmentQueryPort catedra, Clock clock,
                                                                         TurnosProperties turnos) {
        return new RecoveryService(reservations, catedra, clock, turnos.holdRequestTimeout(), Duration.ofMinutes(10));
    }

    /** Se apaga con RECOVERY_ENABLED=false (los tests de integracion no deben tocar la catedra). */
    @Bean
    @ConditionalOnProperty(name = "turnos.recovery-enabled", havingValue = "true", matchIfMissing = true)
    RecoveryRunner recoveryRunner(RecoverPendingReservationsUseCase recovery) {
        return new RecoveryRunner(recovery);
    }
}
