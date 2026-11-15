package com.integrador.turnos.application.port.out;

import com.integrador.turnos.domain.model.User;

/** Como se emite el token de un usuario. Hoy JWT HS256; el servicio no lo sabe. */
public interface TokenIssuerPort {

    String issueFor(User user);
}
