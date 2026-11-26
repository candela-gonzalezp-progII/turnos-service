package com.integrador.turnos.domain;

import com.integrador.turnos.domain.exception.ValidationException;
import com.integrador.turnos.domain.model.FieldError;
import com.integrador.turnos.domain.model.UserRegistration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRegistrationTest {

    @Test
    void normalizaLoginYEmailAMinusculas() {
        UserRegistration r = UserRegistration.of(" Cande_01 ", "clave1234", "Cande", "Gonzalez", "Cande@Mail.COM", null, null);

        assertThat(r.login()).isEqualTo("cande_01");
        assertThat(r.email()).isEqualTo("cande@mail.com");
        assertThat(r.langKey()).isEqualTo("es");
    }

    @Test
    void informaTodosLosCamposInvalidosJuntos() {
        assertThatThrownBy(() -> UserRegistration.of("a", "123", null, null, "no-es-email", null, null))
                .isInstanceOf(ValidationException.class);
        try {
            UserRegistration.of("a", "123", null, null, "no-es-email", null, null);
        } catch (ValidationException e) {
            List<String> fields = e.errors().stream().map(FieldError::field).toList();
            assertThat(fields).containsExactly("login", "password", "email");
        }
    }

    @Test
    void rechazaLoginConCaracteresNoPermitidos() {
        assertThatThrownBy(() -> UserRegistration.of("juan perez", "clave1234", null, null, "j@mail.com", null, null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void toStringNoMuestraLaClave() {
        UserRegistration r = UserRegistration.of("cande", "secreta-123", null, null, "c@mail.com", null, null);

        assertThat(r.toString().contains("secreta-123")).isEqualTo(false);
    }
}
