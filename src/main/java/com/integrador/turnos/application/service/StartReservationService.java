package com.integrador.turnos.application.service;

import com.integrador.turnos.application.exception.CatedraRejectedException;
import com.integrador.turnos.application.exception.InvalidSlotException;
import com.integrador.turnos.application.exception.ProfessionalDisabledException;
import com.integrador.turnos.application.exception.SlotUnavailableException;
import com.integrador.turnos.application.port.in.StartReservationUseCase;
import com.integrador.turnos.application.port.out.CatalogPort;
import com.integrador.turnos.application.port.out.HoldPort;
import com.integrador.turnos.application.port.out.ReservationRepositoryPort;
import com.integrador.turnos.domain.model.ProfessionalSummary;
import com.integrador.turnos.domain.model.Reservation;
import com.integrador.turnos.domain.model.Slot;
import com.integrador.turnos.domain.model.SlotGenerator;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * CU-10. Revalida con catalogo que el horario existe, crea localmente el proceso (HOLD_REQUESTED, ligado
 * al usuario autenticado) y pide el hold a la catedra. Es idempotente: si el usuario ya tiene un proceso
 * vigente para ese horario, se devuelve ese mismo.
 */
public class StartReservationService implements StartReservationUseCase {

    private final CatalogPort catalog;
    private final HoldPort holds;
    private final ReservationRepositoryPort reservations;
    private final Clock clock;
    private final ZoneId zone;

    public StartReservationService(CatalogPort catalog, HoldPort holds, ReservationRepositoryPort reservations,
                                   Clock clock, ZoneId zone) {
        this.catalog = catalog;
        this.holds = holds;
        this.reservations = reservations;
        this.clock = clock;
        this.zone = zone;
    }

    @Override
    public Reservation start(String userLogin, long professionalId, LocalDate date, LocalTime startTime) {
        if (userLogin == null || date == null || startTime == null) {
            throw new IllegalArgumentException("Profesional, fecha y hora son obligatorios");
        }
        Instant now = clock.instant();

        Optional<Reservation> existing = reservations.findActiveForSlot(userLogin, professionalId, date, startTime);
        if (existing.isPresent()) {
            Reservation current = existing.get();
            if (!current.isOverdueAt(now)) {
                return current;                       // reintento del mismo pedido: no se duplica
            }
            current.expire("HOLD_EXPIRED", now);      // el anterior vencio: se cierra y se crea uno nuevo
            reservations.save(current);
        }

        ProfessionalSummary professional = catalog.findProfessional(professionalId);
        if (!professional.enabled()) {
            throw new ProfessionalDisabledException(professionalId);
        }
        Slot slot = SlotGenerator.generate(professional, date).stream()
                .filter(s -> s.start().equals(startTime))
                .findFirst()
                .orElseThrow(() -> new InvalidSlotException("INVALID_SLOT", "El horario no existe en la agenda del profesional"));
        if (LocalDateTime.of(date, startTime).isBefore(LocalDateTime.ofInstant(now, zone))) {
            throw new InvalidSlotException("SLOT_IN_PAST", "El horario ya paso");
        }

        Reservation reservation = reservations.save(Reservation.request(userLogin, professionalId,
                professional.fullName(), date, slot.start(), slot.end(), now));
        return requestHold(reservation, now);
    }

    private Reservation requestHold(Reservation reservation, Instant now) {
        HoldPort.HoldResult hold;
        try {
            hold = holds.createHold(reservation.professionalId(), reservation.date(), reservation.startTime(),
                    reservation.userLogin());
        } catch (CatedraRejectedException e) {
            String code = e.code() == null ? "CATEDRA_REJECTED" : e.code();
            reservation.fail(code, now);
            reservations.save(reservation);
            throw translate(e, code);
        }
        // Si la catedra no respondio (CatedraUnavailableException) el proceso queda HOLD_REQUESTED y
        // el recuperador lo cierra (CU-16): un timeout no prueba que el hold no se haya creado.
        reservation.holdCreated(hold.holdId(), hold.reservationProcessId(), hold.expiresAt(), now);
        return reservations.save(reservation);
    }

    private static RuntimeException translate(CatedraRejectedException e, String code) {
        return switch (code) {
            case "SLOT_ALREADY_HELD", "SLOT_ALREADY_RESERVED" -> new SlotUnavailableException(code);
            case "INVALID_SLOT", "PROFESSIONAL_DISABLED", "PROFESSIONAL_NOT_FOUND" ->
                    new InvalidSlotException(code, "El horario no es valido para este profesional");
            default -> e;
        };
    }
}
