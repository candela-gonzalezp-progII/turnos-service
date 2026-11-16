package com.integrador.turnos.infrastructure.security;

import com.integrador.turnos.application.port.out.PasswordHasherPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Hash BCrypt (con sal propia por clave). Nunca se guarda ni se loguea la clave en texto plano. */
public class BCryptPasswordHasher implements PasswordHasherPort {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hash) {
        return encoder.matches(rawPassword, hash);
    }
}
