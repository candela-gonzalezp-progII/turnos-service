package com.integrador.turnos.infrastructure.web;

/** Cuerpo de POST /api/register. Las reglas de validacion viven en el dominio (UserRegistration). */
public record RegisterRequest(String login, String password, String firstName, String lastName,
                              String email, String imageUrl, String langKey) {

    @Override
    public String toString() {
        return "RegisterRequest[login=" + login + ", email=" + email + ", password=***]";
    }
}
