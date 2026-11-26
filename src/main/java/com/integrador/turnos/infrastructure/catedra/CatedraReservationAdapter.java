package com.integrador.turnos.infrastructure.catedra;

import com.integrador.turnos.application.port.out.AppointmentCancellationPort;
import com.integrador.turnos.application.port.out.AppointmentQueryPort;
import com.integrador.turnos.application.port.out.HoldConfirmationPort;
import com.integrador.turnos.application.port.out.HoldPort;
import com.integrador.turnos.application.port.out.OccupancyPort;
import com.integrador.turnos.domain.model.AppointmentSnapshot;
import com.integrador.turnos.domain.model.Occupancy;
import com.integrador.turnos.infrastructure.catedra.dto.AppointmentResponse;
import com.integrador.turnos.infrastructure.catedra.dto.CancelRequest;
import com.integrador.turnos.infrastructure.catedra.dto.CancelResponse;
import com.integrador.turnos.infrastructure.catedra.dto.ConfirmHoldRequest;
import com.integrador.turnos.infrastructure.catedra.dto.ConfirmHoldResponse;
import com.integrador.turnos.infrastructure.catedra.dto.HoldRequest;
import com.integrador.turnos.infrastructure.catedra.dto.HoldResponse;
import com.integrador.turnos.infrastructure.catedra.dto.OccupancyResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Adapter de salida hacia la API REST de la catedra (ocupaciones, holds, confirmaciones, reservas). */
public class CatedraReservationAdapter implements OccupancyPort, HoldPort, HoldConfirmationPort, AppointmentQueryPort,
        AppointmentCancellationPort {

    /** Tope de seguridad: una cuenta tecnica compartida podria tener muchas reservas; se leen hasta 5 paginas. */
    private static final int PAGE_SIZE = 100;
    private static final int MAX_PAGES = 5;

    private final CatedraHttp http;

    public CatedraReservationAdapter(CatedraHttp http) {
        this.http = http;
    }

    @Override
    public List<Occupancy> findOccupancies(long professionalId, LocalDate from, LocalDate to) {
        OccupancyResponse response = http.get(
                "/api/appointment-occupancies?professionalId={id}&from={from}&to={to}",
                OccupancyResponse.class, professionalId, from, to);
        return response.toDomain();
    }

    @Override
    public HoldResult createHold(long professionalId, LocalDate date, LocalTime startTime, String externalPatientId) {
        HoldResponse r = http.post("/api/appointment-holds",
                new HoldRequest(professionalId, date, startTime, externalPatientId), HoldResponse.class);
        return new HoldResult(r.holdId(), r.reservationProcessId(), r.expiresAt());
    }

    @Override
    public ConfirmResult confirmHold(String holdId, String externalPatientId, String firstName, String lastName) {
        ConfirmHoldResponse r = http.post("/api/appointment-holds/{holdId}/confirm",
                new ConfirmHoldRequest(externalPatientId, firstName, lastName), ConfirmHoldResponse.class, holdId);
        return new ConfirmResult(r.reservationProcessId(), r.status(), r.expiresAt());
    }

    @Override
    public List<AppointmentSnapshot> findByPatient(String externalPatientId) {
        List<AppointmentSnapshot> all = new ArrayList<>();
        for (int page = 0; page < MAX_PAGES; page++) {
            AppointmentResponse[] chunk = http.get("/api/appointments?externalPatientId={p}&page={page}&size={size}",
                    AppointmentResponse[].class, externalPatientId, page, PAGE_SIZE);
            for (AppointmentResponse a : chunk) {
                all.add(a.toDomain());
            }
            if (chunk.length < PAGE_SIZE) {
                break;
            }
        }
        return all;
    }

    @Override
    public CancelResult cancel(String reservationProcessId, String reason) {
        CancelResponse r = http.post("/api/appointments/{processId}/cancel", new CancelRequest(reason),
                CancelResponse.class, reservationProcessId);
        return new CancelResult(r.reservationId(), r.status(), r.cancelledAt());
    }
}
