package com.integrador.turnos.application.service;

import com.integrador.turnos.application.exception.UserNotFoundException;
import com.integrador.turnos.application.port.in.GetAccountUseCase;
import com.integrador.turnos.application.port.out.UserRepositoryPort;
import com.integrador.turnos.domain.model.User;

public class AccountService implements GetAccountUseCase {

    private final UserRepositoryPort users;

    public AccountService(UserRepositoryPort users) {
        this.users = users;
    }

    @Override
    public User getAccount(String login) {
        return users.findByLogin(login).orElseThrow(() -> new UserNotFoundException(login));
    }
}
