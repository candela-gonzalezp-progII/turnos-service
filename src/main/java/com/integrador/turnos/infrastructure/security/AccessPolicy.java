package com.integrador.turnos.infrastructure.security;

import com.integrador.turnos.domain.model.Authorities;

/**
 * Reglas de acceso de turnos-service en un solo lugar (puro, sin Spring, facil de testear).
 * Lo que no esta listado exige estar autenticado como usuario: por defecto se cierra, no se abre.
 */
public class AccessPolicy {

    public Requirement requirementFor(String method, String path) {
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return Requirement.publicAccess();
        }
        if (path.equals("/api/register") || path.equals("/api/authenticate")) {
            return Requirement.publicAccess();
        }
        // Cuenta, disponibilidad y (proximamente) reservas: solo usuarios finales
        return Requirement.anyOf(Authorities.USER, Authorities.ADMIN);
    }
}
