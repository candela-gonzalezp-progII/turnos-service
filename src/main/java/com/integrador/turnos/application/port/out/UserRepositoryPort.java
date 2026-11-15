package com.integrador.turnos.application.port.out;

import com.integrador.turnos.domain.model.User;

import java.util.Optional;

public interface UserRepositoryPort {

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    /** @throws com.integrador.turnos.application.exception.UserAlreadyExistsException si otro registro gana la carrera */
    User save(User user);

    Optional<User> findByLogin(String login);
}
