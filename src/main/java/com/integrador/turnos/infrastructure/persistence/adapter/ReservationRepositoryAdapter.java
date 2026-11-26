package com.integrador.turnos.infrastructure.persistence.adapter;

import com.integrador.turnos.application.exception.ConcurrentReservationUpdateException;
import com.integrador.turnos.application.port.out.ReservationRepositoryPort;
import com.integrador.turnos.domain.model.Reservation;
import com.integrador.turnos.domain.model.ReservationStatus;
import com.integrador.turnos.infrastructure.persistence.jpa.ReservationJpaRepository;
import com.integrador.turnos.infrastructure.persistence.mapper.ReservationMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationRepositoryAdapter implements ReservationRepositoryPort {

    private final ReservationJpaRepository jpa;

    public ReservationRepositoryAdapter(ReservationJpaRepository jpa) {
        this.jpa = jpa;
    }

    /** @Version detecta cambios en simultaneo; el indice unico detecta el doble pedido del mismo horario. */
    @Override
    public Reservation save(Reservation reservation) {
        try {
            return ReservationMapper.toDomain(jpa.saveAndFlush(ReservationMapper.toEntity(reservation)));
        } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException e) {
            throw new ConcurrentReservationUpdateException(e);
        }
    }

    @Override
    public Optional<Reservation> findById(String id) {
        return jpa.findById(id).map(ReservationMapper::toDomain);
    }

    @Override
    public Optional<Reservation> findByIdAndUser(String id, String userLogin) {
        return jpa.findByIdAndUserLogin(id, userLogin).map(ReservationMapper::toDomain);
    }

    @Override
    public Optional<Reservation> findByCatedraProcessId(String catedraProcessId) {
        return jpa.findByCatedraProcessId(catedraProcessId).map(ReservationMapper::toDomain);
    }

    @Override
    public Optional<Reservation> findActiveForSlot(String userLogin, long professionalId, LocalDate date,
                                                   LocalTime startTime) {
        return jpa.findFirstByUserLoginAndProfessionalIdAndDateAndStartTimeAndStatusIn(
                userLogin, professionalId, date, startTime, ReservationStatus.occupying())
                .map(ReservationMapper::toDomain);
    }

    @Override
    public List<Reservation> findByUser(String userLogin) {
        return jpa.findByUserLoginOrderByDateDescStartTimeDesc(userLogin).stream()
                .map(ReservationMapper::toDomain).toList();
    }

    @Override
    public List<Reservation> findPending() {
        return jpa.findByStatusIn(ReservationStatus.pending()).stream().map(ReservationMapper::toDomain).toList();
    }
}
