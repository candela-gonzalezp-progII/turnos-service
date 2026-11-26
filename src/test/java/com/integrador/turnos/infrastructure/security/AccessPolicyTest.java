package com.integrador.turnos.infrastructure.security;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AccessPolicyTest {

    private final AccessPolicy policy = new AccessPolicy();

    @Test
    void registroYLoginSonPublicos() {
        assertThat(policy.requirementFor("POST", "/api/register").isPublic()).isEqualTo(true);
        assertThat(policy.requirementFor("POST", "/api/authenticate").isPublic()).isEqualTo(true);
    }

    @Test
    void optionsEsPublico() {
        assertThat(policy.requirementFor("OPTIONS", "/api/account").isPublic()).isEqualTo(true);
    }

    @Test
    void cuentaYDisponibilidadExigenUsuario() {
        Requirement r = policy.requirementFor("GET", "/api/professionals/3/availability");

        assertThat(r.allows(Set.of("ROLE_USER"))).isEqualTo(true);
        assertThat(r.allows(Set.of("ROLE_SERVICE"))).isEqualTo(false);
        assertThat(policy.requirementFor("GET", "/api/account").allows(Set.of("ROLE_SERVICE"))).isEqualTo(false);
    }

    @Test
    void rutaDesconocidaSeCierraPorDefecto() {
        Requirement r = policy.requirementFor("GET", "/api/lo-que-sea");

        assertThat(r.isPublic()).isEqualTo(false);
        assertThat(r.allows(Set.of())).isEqualTo(false);
    }
}
