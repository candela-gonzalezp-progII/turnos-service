package com.integrador.turnos.application;

import com.integrador.turnos.application.exception.UserAlreadyExistsException;
import com.integrador.turnos.application.port.out.PasswordHasherPort;
import com.integrador.turnos.application.port.out.UserRepositoryPort;
import com.integrador.turnos.application.service.UserRegistrationService;
import com.integrador.turnos.domain.model.Authorities;
import com.integrador.turnos.domain.model.User;
import com.integrador.turnos.domain.model.UserRegistration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRegistrationServiceTest {

    private FakeUsers users;
    private UserRegistrationService service;

    @BeforeEach
    void setUp() {
        users = new FakeUsers();
        PasswordHasherPort hasher = new PasswordHasherPort() {
            public String encode(String raw) { return "hash(" + raw + ")"; }
            public boolean matches(String raw, String hash) { return hash.equals("hash(" + raw + ")"); }
        };
        service = new UserRegistrationService(users, hasher, Clock.fixed(Instant.parse("2026-11-05T10:00:00Z"), ZoneOffset.UTC));
    }

    private static UserRegistration reg(String login, String email) {
        return UserRegistration.of(login, "clave1234", "Cande", "G", email, null, null);
    }

    @Test
    void registraUsuarioActivoConRoleUserYClaveHasheada() {
        User saved = service.register(reg("cande", "c@mail.com"));

        assertThat(saved.isActivated()).isEqualTo(true);
        assertThat(saved.getAuthorities()).containsExactly(Authorities.USER);
        assertThat(saved.getPasswordHash()).isEqualTo("hash(clave1234)");
        assertThat(saved.getCreatedDate()).isEqualTo(Instant.parse("2026-11-05T10:00:00Z"));
    }

    @Test
    void rechazaLoginRepetido() {
        service.register(reg("cande", "c@mail.com"));

        assertThatThrownBy(() -> service.register(reg("cande", "otro@mail.com")))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void rechazaEmailRepetido() {
        service.register(reg("cande", "c@mail.com"));

        assertThatThrownBy(() -> service.register(reg("otra", "c@mail.com")))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void noGuardaNadaSiHayDuplicado() {
        service.register(reg("cande", "c@mail.com"));
        try { service.register(reg("cande", "x@mail.com")); } catch (UserAlreadyExistsException ignored) { }

        assertThat(users.saved).hasSize(1);
    }

    static class FakeUsers implements UserRepositoryPort {
        final List<User> saved = new ArrayList<>();
        public boolean existsByLogin(String l) { return saved.stream().anyMatch(u -> u.getLogin().equals(l)); }
        public boolean existsByEmail(String e) { return saved.stream().anyMatch(u -> u.getEmail().equals(e)); }
        public User save(User u) { saved.add(u); return u; }
        public Optional<User> findByLogin(String l) { return saved.stream().filter(u -> u.getLogin().equals(l)).findFirst(); }
    }
}
