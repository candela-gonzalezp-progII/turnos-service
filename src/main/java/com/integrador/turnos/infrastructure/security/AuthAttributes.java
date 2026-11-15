package com.integrador.turnos.infrastructure.security;

/** Nombres de los atributos de la peticion que deja el filtro tras validar el token. */
public final class AuthAttributes {

    public static final String LOGIN = "turnos.auth.login";
    public static final String AUTHORITIES = "turnos.auth.authorities";

    private AuthAttributes() {
    }
}
