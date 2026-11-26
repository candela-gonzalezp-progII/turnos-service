package com.integrador.turnos.infrastructure.web;

import com.integrador.turnos.application.port.in.GetAccountUseCase;
import com.integrador.turnos.application.port.in.RegisterUserUseCase;
import com.integrador.turnos.infrastructure.security.AuthAttributes;
import com.integrador.turnos.domain.model.UserRegistration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AccountController {

    private final RegisterUserUseCase registerUser;
    private final GetAccountUseCase getAccount;

    public AccountController(RegisterUserUseCase registerUser, GetAccountUseCase getAccount) {
        this.registerUser = registerUser;
        this.getAccount = getAccount;
    }

    /** Cuenta del usuario autenticado (el login sale del token, no de la URL). */
    @GetMapping("/account")
    public AccountResponse account(@RequestAttribute(AuthAttributes.LOGIN) String login) {
        return AccountResponse.from(getAccount.getAccount(login));
    }

    /** CU-01. Publico (no requiere token). 201 sin cuerpo, igual que JHipster. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody RegisterRequest body) {
        registerUser.register(UserRegistration.of(body.login(), body.password(), body.firstName(),
                body.lastName(), body.email(), body.imageUrl(), body.langKey()));
    }
}
