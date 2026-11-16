package com.integrador.turnos.application.port.out;

/** Strategy: como se protege una clave. Hoy BCrypt; el servicio no sabe cual. */
public interface PasswordHasherPort {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String hash);
}
