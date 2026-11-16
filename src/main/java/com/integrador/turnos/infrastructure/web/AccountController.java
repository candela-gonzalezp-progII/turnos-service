package com.integrador.turnos.infrastructure.web;

import com.integrador.turnos.application.port.in.RegisterUserUseCase;
import com.integrador.turnos.domain.model.UserRegistration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AccountController {

    private final RegisterUserUseCase registerUser;

    public AccountController(RegisterUserUseCase registerUser) {
        this.registerUser = registerUser;
    }

    /** CU-01. Publico (no requiere token). 201 sin cuerpo, igual que JHipster. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody RegisterRequest body) {
        registerUser.register(UserRegistration.of(body.login(), body.password(), body.firstName(),
                body.lastName(), body.email(), body.imageUrl(), body.langKey()));
    }
}
