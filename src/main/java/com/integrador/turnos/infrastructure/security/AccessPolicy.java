package com.integrador.turnos.infrastructure.security;

import com.integrador.turnos.domain.model.Authorities;

public class AccessPolicy {

    public Requirement requirementFor(String method, String path) {
        // Permitir preflight requests de CORS sin token
        if ("OPTIONS".equals(method)) {
            return Requirement.publicAccess();
        }

        // Rutas públicas (registro, login y errores)
        if (path.startsWith("/api/account/login") || path.startsWith("/api/account/register") || path.equals("/error")) {
            return Requirement.publicAccess();
        }

        // La sincronización es exclusiva para comunicación entre microservicios
        if (path.startsWith("/api/sync")) {
            return Requirement.anyOf("ROLE_SERVICE");
        }

        // Las consultas generales admiten usuarios, administradores y a otros microservicios
        return Requirement.anyOf(Authorities.USER, Authorities.ADMIN, "ROLE_SERVICE");
    }
}