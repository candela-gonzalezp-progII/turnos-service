package com.integrador.turnos.infrastructure.persistence.jpa;

import com.integrador.turnos.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    Optional<UserEntity> findByLogin(String login);
}
