package com.integrador.turnos.infrastructure.web;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Mismo nombre de campo que JHipster: id_token. */
public record TokenResponse(@JsonProperty("id_token") String idToken) {
}
