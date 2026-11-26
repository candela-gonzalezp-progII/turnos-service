package com.integrador.turnos.infrastructure.catedra.dto;

public record AuthRequest(String username, String password, boolean rememberMe) {
}
