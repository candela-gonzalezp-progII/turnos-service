package com.integrador.turnos.infrastructure.persistence.adapter;

import com.integrador.turnos.application.exception.UserAlreadyExistsException;
import com.integrador.turnos.application.port.out.UserRepositoryPort;
import com.integrador.turnos.domain.model.User;
import com.integrador.turnos.infrastructure.persistence.jpa.UserJpaRepository;
import com.integrador.turnos.infrastructure.persistence.mapper.UserMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpa;

    public UserRepositoryAdapter(UserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public boolean existsByLogin(String login) {
        return jpa.existsByLogin(login);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    /** Si dos registros simultaneos pasan el chequeo previo, la restriccion UNIQUE de la BD decide. */
    @Override
    public User save(User user) {
        try {
            return UserMapper.toDomain(jpa.saveAndFlush(UserMapper.toEntity(user)));
        } catch (DataIntegrityViolationException e) {
            String detail = String.valueOf(e.getMostSpecificCause().getMessage());
            throw new UserAlreadyExistsException(detail.contains("uk_app_user_email") ? "email" : "login");
        }
    }

    @Override
    public Optional<User> findByLogin(String login) {
        return jpa.findByLogin(login).map(UserMapper::toDomain);
    }
}
