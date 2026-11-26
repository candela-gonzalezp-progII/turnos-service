package com.integrador.turnos.infrastructure.persistence.jpa;

import com.integrador.turnos.domain.model.ReservationStatus;
import com.integrador.turnos.infrastructure.persistence.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationJpaRepository extends JpaRepository<ReservationEntity, String> {

    Optional<ReservationEntity> findByIdAndUserLogin(String id, String userLogin);

    Optional<ReservationEntity> findByCatedraProcessId(String catedraProcessId);

    Optional<ReservationEntity> findFirstByUserLoginAndProfessionalIdAndDateAndStartTimeAndStatusIn(
            String userLogin, long professionalId, LocalDate date, LocalTime startTime,
            Collection<ReservationStatus> statuses);

    List<ReservationEntity> findByUserLoginOrderByDateDescStartTimeDesc(String userLogin);

    List<ReservationEntity> findByStatusIn(Collection<ReservationStatus> statuses);
}
