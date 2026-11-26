package com.integrador.turnos.application.port.in;

/** CU-16: converger los procesos no finales con lo que dice la catedra. */
public interface RecoverPendingReservationsUseCase {

    RecoveryReport recover();

    record RecoveryReport(int examined, int updated, boolean catedraReachable) {
    }
}
