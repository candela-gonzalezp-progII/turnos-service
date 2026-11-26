package com.integrador.turnos.application.service;

import com.integrador.turnos.application.exception.ConcurrentReservationUpdateException;
import com.integrador.turnos.application.exception.CatedraAuthenticationException;
import com.integrador.turnos.application.exception.CatedraRejectedException;
import com.integrador.turnos.application.exception.CatedraUnavailableException;
import com.integrador.turnos.application.port.in.MyReservationsUseCase;
import com.integrador.turnos.application.port.out.AppointmentQueryPort;
import com.integrador.turnos.application.port.out.ReservationRepositoryPort;
import com.integrador.turnos.domain.model.AppointmentSnapshot;
import com.integrador.turnos.domain.model.Reservation;
import com.integrador.turnos.domain.model.ReservationReconciler;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * CU-14. Lista SOLO lo del usuario autenticado (el login sale del token). Si la catedra responde, contrasta cada
 * proceso con su reserva y converge el estado local; si no responde, devuelve lo local con verified=false.
 */
public class MyReservationsService implements MyReservationsUseCase {

    private final ReservationRepositoryPort reservations;
    private final AppointmentQueryPort catedra;
    private final Clock clock;

    public MyReservationsService(ReservationRepositoryPort reservations, AppointmentQueryPort catedra, Clock clock) {
        this.reservations = reservations;
        this.catedra = catedra;
        this.clock = clock;
    }

    @Override
    public MyReservations list(String userLogin) {
        List<Reservation> local = reservations.findByUser(userLogin);
        if (local.isEmpty()) {
            return new MyReservations(List.of(), true);
        }
        Instant now = clock.instant();
        Map<String, AppointmentSnapshot> remote;
        boolean verified = true;
        try {
            remote = catedra.findByPatient(userLogin).stream()
                    .filter(a -> a.reservationProcessId() != null)
                    .collect(Collectors.toMap(AppointmentSnapshot::reservationProcessId, Function.identity(), (a, b) -> a));
        } catch (CatedraUnavailableException | CatedraAuthenticationException | CatedraRejectedException e) {
            remote = Map.of();
            verified = false;
        }
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : local) {
            result.add(refresh(r, remote.get(r.catedraProcessId()), now));
        }
        return new MyReservations(result, verified);
    }

    private Reservation refresh(Reservation r, AppointmentSnapshot snapshot, Instant now) {
        try {
            boolean changed = false;
            if (snapshot != null) {
                changed = ReservationReconciler.reconcile(r, snapshot, now);
            }
            if (!changed && r.isOverdueAt(now) && r.canTransitionTo(com.integrador.turnos.domain.model.ReservationStatus.EXPIRED)) {
                r.expire("HOLD_EXPIRED", now);
                changed = true;
            }
            return changed ? reservations.save(r) : r;
        } catch (ConcurrentReservationUpdateException e) {
            return reservations.findById(r.id()).orElse(r);     // otro proceso la actualizo: se muestra lo mas nuevo
        }
    }
}
