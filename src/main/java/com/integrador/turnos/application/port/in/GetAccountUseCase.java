package com.integrador.turnos.application.port.in;

import com.integrador.turnos.domain.model.User;

public interface GetAccountUseCase {

    User getAccount(String login);
}
