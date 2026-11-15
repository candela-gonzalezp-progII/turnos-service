package com.integrador.turnos.infrastructure.security;

import com.integrador.turnos.domain.model.Authorities;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/**
 * Token tecnico para que turnos llame al catalogo: ROLE_SERVICE, vida corta. Si hay una peticion de
 * usuario en curso, deja su login en "obo" (on behalf of) para trazabilidad.
 */
public class ServiceTokenProvider {

    static final String SERVICE_NAME = "turnos-service";

    private final HmacJwtService jwt;
    private final Duration ttl;

    public ServiceTokenProvider(HmacJwtService jwt, Duration ttl) {
        this.jwt = jwt;
        this.ttl = ttl;
    }

    public String token() {
        return token(currentUser());
    }

    String token(String onBehalfOf) {
        Instant now = jwt.clock().instant();
        return jwt.sign(new JwtClaims(SERVICE_NAME, Set.of(Authorities.SERVICE), jwt.issuer(), now,
                now.plus(ttl), onBehalfOf));
    }

    private static String currentUser() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        Object login = attrs.getAttribute(AuthAttributes.LOGIN, RequestAttributes.SCOPE_REQUEST);
        return login instanceof String s ? s : null;
    }
}
