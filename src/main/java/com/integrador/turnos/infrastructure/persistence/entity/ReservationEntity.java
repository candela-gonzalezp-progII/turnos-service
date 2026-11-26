package com.integrador.turnos.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import com.integrador.turnos.domain.model.ReservationStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "reservation")
public class ReservationEntity {

    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "user_login", nullable = false, length = 50)
    private String userLogin;
    @Column(name = "professional_id", nullable = false)
    private long professionalId;
    @Column(name = "professional_name", nullable = false, length = 200)
    private String professionalName;
    @Column(name = "appointment_date", nullable = false)
    private LocalDate date;
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status;
    @Column(name = "hold_id", length = 100)
    private String holdId;
    @Column(name = "catedra_process_id", length = 100)
    private String catedraProcessId;
    @Column(name = "hold_expires_at")
    private Instant holdExpiresAt;
    @Column(name = "patient_first_name", length = 100)
    private String patientFirstName;
    @Column(name = "patient_last_name", length = 100)
    private String patientLastName;
    @Column(name = "patient_phone", length = 30)
    private String patientPhone;
    @Column(name = "request_event_id", length = 100)
    private String requestEventId;
    @Column(name = "catedra_reservation_id")
    private Long catedraReservationId;
    @Column(length = 100)
    private String reason;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Version
    private Long version;

    protected ReservationEntity() {
    }

    public ReservationEntity(String id, String userLogin, long professionalId, String professionalName, LocalDate date,
                             LocalTime startTime, LocalTime endTime, ReservationStatus status, String holdId,
                             String catedraProcessId, Instant holdExpiresAt, String patientFirstName,
                             String patientLastName, String patientPhone, String requestEventId,
                             Long catedraReservationId, String reason, Instant createdAt, Instant updatedAt,
                             Long version) {
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

    public String getId() { return id; }
    public String getUserLogin() { return userLogin; }
    public long getProfessionalId() { return professionalId; }
    public String getProfessionalName() { return professionalName; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public ReservationStatus getStatus() { return status; }
    public String getHoldId() { return holdId; }
    public String getCatedraProcessId() { return catedraProcessId; }
    public Instant getHoldExpiresAt() { return holdExpiresAt; }
    public String getPatientFirstName() { return patientFirstName; }
    public String getPatientLastName() { return patientLastName; }
    public String getPatientPhone() { return patientPhone; }
    public String getRequestEventId() { return requestEventId; }
    public Long getCatedraReservationId() { return catedraReservationId; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}
