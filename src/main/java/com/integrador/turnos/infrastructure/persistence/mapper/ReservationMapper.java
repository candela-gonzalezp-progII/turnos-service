package com.integrador.turnos.infrastructure.persistence.mapper;

import com.integrador.turnos.domain.model.Reservation;
import com.integrador.turnos.infrastructure.persistence.entity.ReservationEntity;

public final class ReservationMapper {

    private ReservationMapper() {
    }

    public static Reservation toDomain(ReservationEntity e) {
        return new Reservation(e.getId(), e.getUserLogin(), e.getProfessionalId(), e.getProfessionalName(),
                e.getDate(), e.getStartTime(), e.getEndTime(), e.getStatus(), e.getHoldId(), e.getCatedraProcessId(),
                e.getHoldExpiresAt(), e.getPatientFirstName(), e.getPatientLastName(), e.getPatientPhone(),
                e.getRequestEventId(), e.getCatedraReservationId(), e.getReason(), e.getCreatedAt(),
                e.getUpdatedAt(), e.getVersion());
    }

    public static ReservationEntity toEntity(Reservation r) {
        return new ReservationEntity(r.id(), r.userLogin(), r.professionalId(), r.professionalName(), r.date(),
                r.startTime(), r.endTime(), r.status(), r.holdId(), r.catedraProcessId(), r.holdExpiresAt(),
                r.patientFirstName(), r.patientLastName(), r.patientPhone(), r.requestEventId(),
                r.catedraReservationId(), r.reason(), r.createdAt(), r.updatedAt(), r.version());
    }
}
