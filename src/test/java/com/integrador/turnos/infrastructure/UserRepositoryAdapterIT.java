package com.integrador.turnos.infrastructure;

import com.integrador.turnos.AbstractPostgresIT;
import com.integrador.turnos.application.exception.UserAlreadyExistsException;
import com.integrador.turnos.application.port.out.UserRepositoryPort;
import com.integrador.turnos.domain.model.User;
import com.integrador.turnos.domain.model.UserRegistration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRepositoryAdapterIT extends AbstractPostgresIT {

    @Autowired
    private UserRepositoryPort users;

    private static User user(String login, String email) {
        return User.register(UserRegistration.of(login, "clave1234", "A", "B", email, null, null),
                "$2a$10$abcdefghijklmnopqrstuuabcdefghijklmnopqrstuvwxyz01234", Instant.now());
    }

    @Test
    void guardaYRecuperaConSusAutoridades() {
        User saved = users.save(user("it_user1", "it1@mail.com"));

        User found = users.findByLogin("it_user1").orElseThrow();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getAuthorities()).contains("ROLE_USER");
        assertThat(users.existsByEmail("it1@mail.com")).isEqualTo(true);
    }

    @Test
    void laBaseRechazaLoginDuplicadoAunSinChequeoPrevio() {
        users.save(user("it_user2", "it2@mail.com"));

        assertThatThrownBy(() -> users.save(user("it_user2", "it2b@mail.com")))
                .isInstanceOf(UserAlreadyExistsException.class);
    }
}