package com.integrador.turnos.domain.model;

import com.integrador.turnos.domain.exception.InvalidReservationStateException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Proceso de reserva de un usuario final. Guarda el estado local y los identificadores de la catedra
 * (holdId, reservationProcessId) para poder reconciliar y recuperarse tras un reinicio.
 * Toda transicion pasa por la maquina de estados de {@link ReservationStatus}.
 * El vencimiento (holdExpiresAt) lo fija la catedra: conservarlo localmente no lo extiende.
 */
public class Reservation {

    static final java.time.Duration SUBMITTED_GRACE = java.time.Duration.ofMinutes(2);

    private final String id;
    private final String userLogin;
    private final long professionalId;
    private final String professionalName;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Instant createdAt;
    private final Long version;

    private ReservationStatus status;
    private String holdId;
    private String catedraProcessId;
    private Instant holdExpiresAt;
    private String patientFirstName;
    private String patientLastName;
    private String patientPhone;
    private String requestEventId;
    private Long catedraReservationId;
    private String reason;
    private Instant updatedAt;

    /** Rehidratacion desde la persistencia. */
    public Reservation(String id, String userLogin, long professionalId, String professionalName, LocalDate date,
                       LocalTime startTime, LocalTime endTime, ReservationStatus status, String holdId,
                       String catedraProcessId, Instant holdExpiresAt, String patientFirstName,
                       String patientLastName, String patientPhone, String requestEventId,
                       Long catedraReservationId, String reason, Instant createdAt, Instant updatedAt, Long version) {
        this.id = id;
        this.userLogin = userLogin;
        this.professionalId = professionalId;
        this.professionalName = professionalName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.holdId = holdId;
        this.catedraProcessId = catedraProcessId;
        this.holdExpiresAt = holdExpiresAt;
        this.patientFirstName = patientFirstName;
        this.patientLastName = patientLastName;
        this.patientPhone = patientPhone;
        this.requestEventId = requestEventId;
        this.catedraReservationId = catedraReservationId;
        this.reason = reason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    /** Nuevo proceso, ligado al usuario autenticado, todavia sin respuesta de la catedra. */
    public static Reservation request(String userLogin, long professionalId, String professionalName,
                                      LocalDate date, LocalTime startTime, LocalTime endTime, Instant now) {
        return new Reservation(UUID.randomUUID().toString(), userLogin, professionalId, professionalName, date,
                startTime, endTime, ReservationStatus.HOLD_REQUESTED, null, null, null, null, null, null, null,
                null, null, now, now, null);
    }

    // ---- transiciones guiadas por REST ----

    public void holdCreated(String holdId, String catedraProcessId, Instant expiresAt, Instant now) {
        move(ReservationStatus.HOLD_CREATED, now);
        this.holdId = holdId;
        this.catedraProcessId = catedraProcessId;
        this.holdExpiresAt = expiresAt;
    }

    public void confirmationStarted(PatientName name, Instant now) {
        move(ReservationStatus.CONFIRMATION_PENDING, now);
        this.patientFirstName = name.firstName();
        this.patientLastName = name.lastName();
    }

    public void infoSubmitted(String phone, Instant now) {
        move(ReservationStatus.INFO_SUBMITTED, now);
        this.patientPhone = phone;
    }

    // ---- transiciones guiadas por eventos (Kafka) o por reconciliacion ----

    public void infoRequested(String requestEventId, Instant expiresAt, Instant now) {
        move(ReservationStatus.INFO_REQUESTED, now);
        this.requestEventId = requestEventId;
        if (expiresAt != null) {
            this.holdExpiresAt = expiresAt;
        }
    }

    public void infoRejected(String rejectionReason, Instant expiresAt, Instant now) {
        move(ReservationStatus.INFO_REJECTED, now);
        this.reason = rejectionReason;
        if (expiresAt != null) {
            this.holdExpiresAt = expiresAt;
        }
    }

    public void confirmed(Long catedraReservationId, Instant now) {
        move(ReservationStatus.CONFIRMED, now);
        this.catedraReservationId = catedraReservationId;
        this.reason = null;
    }

    public void expire(String expireReason, Instant now) {
        move(ReservationStatus.EXPIRED, now);
        this.reason = expireReason;
    }

    public void invalidate(String invalidReason, Instant now) {
        move(ReservationStatus.INVALID, now);
        this.reason = invalidReason;
    }

    public void fail(String failureCode, Instant now) {
        move(ReservationStatus.FAILED, now);
        this.reason = failureCode;
    }

    public void cancel(Instant now) {
        move(ReservationStatus.CANCELLED, now);
    }

    // ---- consultas ----

    public boolean canTransitionTo(ReservationStatus target) {
        return status.canTransitionTo(target);
    }

    public boolean isFinal() {
        return status.isFinal();
    }

    public boolean isPending() {
        return status.isPending();
    }

    /** El bloqueo sigue vigente en {@code now} segun el vencimiento que informo la catedra. */
    public boolean isHoldValidAt(Instant now) {
        return holdExpiresAt != null && holdExpiresAt.isAfter(now);
    }

    /**
     * Proceso pendiente cuyo vencimiento ya paso. Si el telefono ya se envio (INFO_SUBMITTED) se espera un
     * margen de 2 minutos: la confirmacion puede estar en camino y un estado final no se reabre.
     */
    public boolean isOverdueAt(Instant now) {
        if (!isPending() || holdExpiresAt == null) {
            return false;
        }
        Instant limit = status == ReservationStatus.INFO_SUBMITTED ? holdExpiresAt.plus(SUBMITTED_GRACE) : holdExpiresAt;
        return !limit.isAfter(now);
    }

    /** La app debe pedirle el telefono al usuario. */
    public boolean requiresPhone() {
        return status == ReservationStatus.INFO_REQUESTED || status == ReservationStatus.INFO_REJECTED;
    }

    private void move(ReservationStatus target, Instant now) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidReservationStateException(status, target);
        }
        this.status = target;
        this.updatedAt = now;
    }

    public String id() { return id; }
    public String userLogin() { return userLogin; }
    public long professionalId() { return professionalId; }
    public String professionalName() { return professionalName; }
    public LocalDate date() { return date; }
    public LocalTime startTime() { return startTime; }
    public LocalTime endTime() { return endTime; }
    public ReservationStatus status() { return status; }
    public String holdId() { return holdId; }
    public String catedraProcessId() { return catedraProcessId; }
    public Instant holdExpiresAt() { return holdExpiresAt; }
    public String patientFirstName() { return patientFirstName; }
    public String patientLastName() { return patientLastName; }
    public String patientPhone() { return patientPhone; }
    public String requestEventId() { return requestEventId; }
    public Long catedraReservationId() { return catedraReservationId; }
    public String reason() { return reason; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public Long version() { return version; }

    /** Sin datos personales: nombre del paciente y telefono nunca salen en un log. */
    @Override
    public String toString() {
        return "Reservation[id=" + id + ", user=" + userLogin + ", status=" + status + ", professional="
                + professionalId + ", date=" + date + " " + startTime + "]";
    }
}
