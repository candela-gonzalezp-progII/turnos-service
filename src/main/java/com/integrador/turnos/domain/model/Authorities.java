package com.integrador.turnos.domain.model;

/** Autoridades del sistema. Las asigna siempre el backend; el cliente no las elige. */
public final class Authorities {

    public static final String USER = "ROLE_USER";
    public static final String ADMIN = "ROLE_ADMIN";
    /** Solo para tokens tecnicos entre servicios; no se asigna a ningun usuario. */
    public static final String SERVICE = "ROLE_SERVICE";

    private Authorities() {
    }
}
