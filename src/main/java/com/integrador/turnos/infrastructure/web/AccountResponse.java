package com.integrador.turnos.infrastructure.web;

import com.integrador.turnos.domain.model.User;

import java.util.Set;

/** Datos publicos de la cuenta. Nunca incluye el hash de la clave. */
public record AccountResponse(Long id, String login, String firstName, String lastName, String email,
                              String imageUrl, boolean activated, String langKey, Set<String> authorities) {

    public static AccountResponse from(User u) {
        return new AccountResponse(u.getId(), u.getLogin(), u.getFirstName(), u.getLastName(), u.getEmail(),
                u.getImageUrl(), u.isActivated(), u.getLangKey(), u.getAuthorities());
    }
}
