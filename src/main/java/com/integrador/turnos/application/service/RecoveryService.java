package com.integrador.turnos.application.service;

import com.integrador.turnos.application.exception.CatedraAuthenticationException;
import com.integrador.turnos.application.exception.CatedraRejectedException;
import com.integrador.turnos.application.exception.CatedraUnavailableException;
import com.integrador.turnos.application.exception.ConcurrentReservationUpdateException;
import com.integrador.turnos.application.port.in.RecoverPendingReservationsUseCase;
import com.integrador.turnos.application.port.out.AppointmentQueryPort;
import com.integrador.turnos.application.port.out.ReservationRepositoryPort;
import com.integrador.turnos.domain.model.AppointmentSnapshot;
import com.integrador.turnos.domain.model.Reservation;
import com.integrador.turnos.domain.model.ReservationReconciler;
import com.integrador.turnos.domain.model.ReservationStatus;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CU-16. Recorre los procesos no finales (al arrancar y cada tanto) y los lleva a un estado correcto:
 *  - HOLD_REQUESTED sin respuesta (timeout/caida): pasado {@code holdRequestTimeout} se cierra como FAILED; el hold
 *    que pudo haber quedado en la catedra vence solo (la catedra no permite consultar un hold sin su id).
 *  - Con reservationProcessId: se consulta a la catedra (un timeout no prueba que algo fallo) y se converge.
 *  - Vencido: se cierra como EXPIRED solo despues de consultar a la catedra; si esta caida se espera un margen
 *    ({@code grace}) para no cerrar un proceso que quiza ya estaba confirmado.
 * Nunca hay ciclos: cada pasada hace como mucho una consulta por usuario y los errores dejan el proceso pendiente.
 */
public class RecoveryService implements RecoverPendingReservationsUseCase {

    private final ReservationRepositoryPort reservations;
    private final AppointmentQueryPort catedra;
    private final Clock clock;
    private final Duration holdRequestTimeout;
    private final Duration grace;

    public RecoveryService(ReservationRepositoryPort reservations, AppointmentQueryPort catedra, Clock clock,
                           Duration holdRequestTimeout, Duration grace) {
        this.reservations = reservations;
        this.catedra = catedra;
        this.clock = clock;
        this.holdRequestTimeout = holdRequestTimeout;
        this.grace = grace;
    }

    @Override
    public RecoveryReport recover() {
        Instant now = clock.instant();
        List<Reservation> pending = reservations.findPending();
        Map<String, Map<String, AppointmentSnapshot>> remoteByUser = new HashMap<>();
        boolean reachable = true;
        int updated = 0;

        for (Reservation r : pending) {
            Map<String, AppointmentSnapshot> remote = null;
            if (r.catedraProcessId() != null && reachable) {
                remote = remoteByUser.get(r.userLogin());
                if (remote == null) {
                    try {
                        remote = new HashMap<>();
                        for (AppointmentSnapshot a : catedra.findByPatient(r.userLogin())) {
                            if (a.reservationProcessId() != null) {
                                remote.put(a.reservationProcessId(), a);
                            }
                        }
                        remoteByUser.put(r.userLogin(), remote);
                    } catch (CatedraUnavailableException | CatedraAuthenticationException | CatedraRejectedException e) {
                        reachable = false;          // no se insiste en esta pasada: se reintenta en la proxima
                        remote = null;
                    }
                }
            }
            try {
                if (recoverOne(r, remote, reachable, now)) {
                    updated++;
                }
            } catch (ConcurrentReservationUpdateException e) {
                // otro proceso (evento Kafka, usuario) la actualizo: la proxima pasada la ve al dia
            }
        }
        return new RecoveryReport(pending.size(), updated, reachable);
    }

    private boolean recoverOne(Reservation r, Map<String, AppointmentSnapshot> remote, boolean reachable, Instant now) {
        boolean changed = false;
        if (r.status() == ReservationStatus.HOLD_REQUESTED) {
            if (!r.createdAt().plus(holdRequestTimeout).isAfter(now)) {
                r.fail("HOLD_NOT_CONFIRMED", now);
                changed = true;
            }
        } else {
            if (remote != null) {
                AppointmentSnapshot snapshot = remote.get(r.catedraProcessId());
                if (snapshot != null) {
                    changed = ReservationReconciler.reconcile(r, snapshot, now);
                }
            }
            boolean overdue = r.isPending() && r.holdExpiresAt() != null && !r.holdExpiresAt().isAfter(now);
            boolean safeToClose = r.isOverdueAt(now) && (reachable || !r.holdExpiresAt().plus(grace).isAfter(now));
            if (r.isPending() && overdue && safeToClose) {
                r.expire("HOLD_EXPIRED", now);
                changed = true;
            }
        }
        if (changed) {
            reservations.save(r);
        }
        return changed;
    }
}
