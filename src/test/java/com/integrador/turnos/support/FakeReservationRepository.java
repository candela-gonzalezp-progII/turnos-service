package com.integrador.turnos.support;

import com.integrador.turnos.application.exception.ConcurrentReservationUpdateException;
import com.integrador.turnos.application.port.out.ReservationRepositoryPort;
import com.integrador.turnos.domain.model.Reservation;
import com.integrador.turnos.domain.model.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Repositorio en memoria para tests de servicios. Simula el bloqueo optimista con la version. */
public class FakeReservationRepository implements ReservationRepositoryPort {

    private final Map<String, Reservation> data = new LinkedHashMap<>();
    /** Cantidad de proximos save() que fallan por concurrencia (para probar reintentos). */
    public int failNextSaves = 0;
    public int saves = 0;

    @Override
    public Reservation save(Reservation r) {
        if (failNextSaves > 0) {
            failNextSaves--;
            throw new ConcurrentReservationUpdateException(null);
        }
        saves++;
        long v = r.version() == null ? 0 : r.version() + 1;
        Reservation stored = new Reservation(r.id(), r.userLogin(), r.professionalId(), r.professionalName(), r.date(),
                r.startTime(), r.endTime(), r.status(), r.holdId(), r.catedraProcessId(), r.holdExpiresAt(),
                r.patientFirstName(), r.patientLastName(), r.patientPhone(), r.requestEventId(),
                r.catedraReservationId(), r.reason(), r.createdAt(), r.updatedAt(), v);
        data.put(r.id(), stored);
        return stored;
    }

    @Override
    public Optional<Reservation> findById(String id) {
        return Optional.ofNullable(data.get(id)).map(FakeReservationRepository::copy);
    }

    @Override
    public Optional<Reservation> findByIdAndUser(String id, String userLogin) {
        return findById(id).filter(r -> r.userLogin().equals(userLogin));
    }

    @Override
    public Optional<Reservation> findByCatedraProcessId(String processId) {
        return data.values().stream().filter(r -> processId.equals(r.catedraProcessId())).findFirst()
                .map(FakeReservationRepository::copy);
    }

    @Override
    public Optional<Reservation> findActiveForSlot(String user, long professionalId, LocalDate date, LocalTime start) {
        return data.values().stream()
                .filter(r -> r.userLogin().equals(user) && r.professionalId() == professionalId
                        && r.date().equals(date) && r.startTime().equals(start) && r.status().occupiesSlot())
                .findFirst().map(FakeReservationRepository::copy);
    }

    @Override
    public List<Reservation> findByUser(String user) {
        return data.values().stream().filter(r -> r.userLogin().equals(user)).map(FakeReservationRepository::copy).toList();
    }

    @Override
    public List<Reservation> findPending() {
        return new ArrayList<>(data.values().stream().filter(r -> r.status().isPending())
                .map(FakeReservationRepository::copy).toList());
    }

    public List<Reservation> all() {
        return data.values().stream().map(FakeReservationRepository::copy).toList();
    }

    public Reservation only() {
        return all().get(0);
    }

    private static Reservation copy(Reservation r) {
        return new Reservation(r.id(), r.userLogin(), r.professionalId(), r.professionalName(), r.date(),
                r.startTime(), r.endTime(), r.status(), r.holdId(), r.catedraProcessId(), r.holdExpiresAt(),
                r.patientFirstName(), r.patientLastName(), r.patientPhone(), r.requestEventId(),
                r.catedraReservationId(), r.reason(), r.createdAt(), r.updatedAt(), r.version());
    }

    public static ReservationStatus statusOf(Reservation r) {
        return r.status();
    }
}
