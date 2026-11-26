package com.integrador.turnos.domain;

import com.integrador.turnos.domain.exception.ValidationException;
import com.integrador.turnos.domain.model.PhoneNumber;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNumberTest {

    @Test
    void limpiaEspaciosGuionesParentesisYPuntos() {
        assertThat(PhoneNumber.of("+54 (261) 555-1234").value()).isEqualTo("+542615551234");
        assertThat(PhoneNumber.of("261.555.1234").value()).isEqualTo("2615551234");
    }

    @Test
    void rechazaLoQueLaCatedraRechazaria() {
        assertThatThrownBy(() -> PhoneNumber.of("123456")).isInstanceOf(ValidationException.class);          // 6 digitos
        assertThatThrownBy(() -> PhoneNumber.of("1234567890123456")).isInstanceOf(ValidationException.class); // 16
        assertThatThrownBy(() -> PhoneNumber.of("26155abc123")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> PhoneNumber.of(" ")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> PhoneNumber.of(null)).isInstanceOf(ValidationException.class);
    }

    @Test
    void aceptaLosLimites() {
        assertThat(PhoneNumber.of("1234567").value()).isEqualTo("1234567");
        assertThat(PhoneNumber.of("+123456789012345").value()).isEqualTo("+123456789012345");
    }

    @Test
    void toStringNoMuestraElNumero() {
        assertThat(PhoneNumber.of("+5492615551234").toString().contains("549261555")).isFalse();
    }
}
