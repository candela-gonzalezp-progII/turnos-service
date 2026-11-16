package com.integrador.turnos.application.service;

import com.integrador.turnos.application.exception.InvalidCredentialsException;
import com.integrador.turnos.application.port.in.AuthenticateUserUseCase;
import com.integrador.turnos.application.port.out.PasswordHasherPort;
import com.integrador.turnos.application.port.out.TokenIssuerPort;
import com.integrador.turnos.application.port.out.UserRepositoryPort;
import com.integrador.turnos.domain.model.User;

import java.util.Locale;

/** CU-02. Mismo error para usuario inexistente, clave incorrecta o cuenta inactiva (no filtra cual fue). */
public class AuthenticationService implements AuthenticateUserUseCase {

    private final UserRepositoryPort users;
    private final PasswordHasherPort hasher;
    private final TokenIssuerPort tokens;

    public AuthenticationService(UserRepositoryPort users, PasswordHasherPort hasher, TokenIssuerPort tokens) {
        this.users = users;
        this.hasher = hasher;
        this.tokens = tokens;
    }

    @Override
    public String authenticate(String login, String password) {
        if (login == null || password == null || login.isBlank() || password.isEmpty()) {
            throw new InvalidCredentialsException();
        }
        User user = users.findByLogin(login.trim().toLowerCase(Locale.ROOT))
                .filter(User::isActivated)
                .filter(u -> hasher.matches(password, u.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        return tokens.issueFor(user);
    }
}
