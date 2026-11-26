package com.integrador.turnos.application;

import com.integrador.turnos.application.exception.InvalidCredentialsException;
import com.integrador.turnos.application.port.out.PasswordHasherPort;
import com.integrador.turnos.application.port.out.TokenIssuerPort;
import com.integrador.turnos.application.port.out.UserRepositoryPort;
import com.integrador.turnos.application.service.AuthenticationService;
import com.integrador.turnos.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticationServiceTest {

    private AuthenticationService service;
    private boolean activated;

    @BeforeEach
    void setUp() {
        activated = true;
        UserRepositoryPort users = new UserRepositoryPort() {
            public boolean existsByLogin(String l) { return false; }
            public boolean existsByEmail(String e) { return false; }
            public User save(User u) { return u; }
            public Optional<User> findByLogin(String l) {
                return l.equals("cande")
                        ? Optional.of(new User(1L, "cande", "hash(ok)", null, null, "c@mail.com", null, "es", activated, Set.of("ROLE_USER"), Instant.now()))
                        : Optional.empty();
            }
        };
        PasswordHasherPort hasher = new PasswordHasherPort() {
            public String encode(String raw) { return "hash(" + raw + ")"; }
            public boolean matches(String raw, String hash) { return hash.equals("hash(" + raw + ")"); }
        };
        TokenIssuerPort tokens = user -> "token-de-" + user.getLogin();
        service = new AuthenticationService(users, hasher, tokens);
    }

    @Test
    void credencialesCorrectasDevuelvenToken() {
        assertThat(service.authenticate("Cande", "ok")).isEqualTo("token-de-cande");
    }

    @Test
    void claveIncorrecta() {
        assertThatThrownBy(() -> service.authenticate("cande", "mala")).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void usuarioInexistente() {
        assertThatThrownBy(() -> service.authenticate("nadie", "ok")).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void cuentaInactiva() {
        activated = false;

        assertThatThrownBy(() -> service.authenticate("cande", "ok")).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void camposVacios() {
        assertThatThrownBy(() -> service.authenticate("", "ok")).isInstanceOf(InvalidCredentialsException.class);
        assertThatThrownBy(() -> service.authenticate("cande", null)).isInstanceOf(InvalidCredentialsException.class);
    }
}
