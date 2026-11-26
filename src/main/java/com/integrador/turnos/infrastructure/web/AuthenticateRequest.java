package com.integrador.turnos.infrastructure.web;

public record AuthenticateRequest(String username, String password) {

    @Override
    public String toString() {
        return "AuthenticateRequest[username=" + username + ", password=***]";
    }
}
