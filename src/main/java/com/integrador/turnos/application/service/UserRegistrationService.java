package com.integrador.turnos.application.service;

import com.integrador.turnos.application.exception.UserAlreadyExistsException;
import com.integrador.turnos.application.port.in.RegisterUserUseCase;
import com.integrador.turnos.application.port.out.PasswordHasherPort;
import com.integrador.turnos.application.port.out.UserRepositoryPort;
import com.integrador.turnos.domain.model.User;
import com.integrador.turnos.domain.model.UserRegistration;

import java.time.Clock;

/**
 * CU-01. Los datos ya vienen validados (UserRegistration). Acá: unicidad de login y email, hash de la
 * clave, y alta con las decisiones del backend (activo, ROLE_USER).
 */
public class UserRegistrationService implements RegisterUserUseCase {

    private final UserRepositoryPort users;
    private final PasswordHasherPort hasher;
    private final Clock clock;

    public UserRegistrationService(UserRepositoryPort users, PasswordHasherPort hasher, Clock clock) {
        this.users = users;
        this.hasher = hasher;
        this.clock = clock;
    }

    @Override
    public User register(UserRegistration registration) {
        if (users.existsByLogin(registration.login())) {
            throw new UserAlreadyExistsException("login");
        }
        if (users.existsByEmail(registration.email())) {
            throw new UserAlreadyExistsException("email");
        }
        String hash = hasher.encode(registration.password());
        return users.save(User.register(registration, hash, clock.instant()));
    }
}
