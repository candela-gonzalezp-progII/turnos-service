package com.integrador.turnos.infrastructure.web;

import com.integrador.turnos.application.port.in.AuthenticateUserUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthenticateUserUseCase authenticate;

    public AuthController(AuthenticateUserUseCase authenticate) {
        this.authenticate = authenticate;
    }

    /** CU-02. Publico. 200 con {"id_token": "..."}; 401 si las credenciales no son validas. */
    @PostMapping("/authenticate")
    public TokenResponse authenticate(@RequestBody AuthenticateRequest body) {
        return new TokenResponse(authenticate.authenticate(body.username(), body.password()));
    }
}
