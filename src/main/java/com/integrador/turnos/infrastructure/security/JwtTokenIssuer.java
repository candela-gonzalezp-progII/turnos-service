package com.integrador.turnos.infrastructure.security;

import com.integrador.turnos.application.port.out.TokenIssuerPort;
import com.integrador.turnos.domain.model.User;

import java.time.Duration;
import java.time.Instant;

/** Token de usuario: sub = login, auth = sus autoridades, vence segun security.jwt.expiration. */
public class JwtTokenIssuer implements TokenIssuerPort {

    private final HmacJwtService jwt;
    private final Duration expiration;

    public JwtTokenIssuer(HmacJwtService jwt, Duration expiration) {
        this.jwt = jwt;
        this.expiration = expiration;
    }

    @Override
    public String issueFor(User user) {
        Instant now = jwt.clock().instant();
        return jwt.sign(new JwtClaims(user.getLogin(), user.getAuthorities(), jwt.issuer(), now,
                now.plus(expiration), null));
    }
}
