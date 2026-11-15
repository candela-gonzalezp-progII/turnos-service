package com.integrador.turnos.application.port.in;

import com.integrador.turnos.domain.model.User;
import com.integrador.turnos.domain.model.UserRegistration;

/** CU-01: registrar un usuario final. */
public interface RegisterUserUseCase {

    User register(UserRegistration registration);
}
