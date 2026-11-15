package com.integrador.turnos.application.port.in;

/** CU-02: autenticar con login y clave. Devuelve el token (JWT). */
public interface AuthenticateUserUseCase {

    String authenticate(String login, String password);
}
