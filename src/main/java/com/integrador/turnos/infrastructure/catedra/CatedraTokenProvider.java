package com.integrador.turnos.infrastructure.catedra;

import com.integrador.turnos.application.exception.CatedraAuthenticationException;
import com.integrador.turnos.infrastructure.catedra.dto.AuthRequest;
import com.integrador.turnos.infrastructure.catedra.dto.AuthResponse;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * JWT tecnico de la catedra (una sola cuenta compartida por catalogo y turnos). Se obtiene con POST
 * /api/authenticate y se guarda en memoria; nunca se loguea ni se escribe en disco.
 */
public class CatedraTokenProvider {

    private final RestClient client;
    private final CatedraProperties props;
    private final RetryExecutor retry;
    private volatile String token;

    public CatedraTokenProvider(RestClient client, CatedraProperties props, RetryExecutor retry) {
        this.client = client;
        this.props = props;
        this.retry = retry;
    }

    public String bearerToken() {
        String current = token;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (token == null) {
                token = login();
            }
            return token;
        }
    }

    public synchronized void invalidate() {
        token = null;
    }

    private String login() {
        if (props.username() == null || props.username().isBlank() || props.password() == null
                || props.password().isBlank()) {
            throw new CatedraAuthenticationException("Faltan CATEDRA_USERNAME / CATEDRA_PASSWORD en el .env", null);
        }
        AuthResponse response = retry.execute(() -> {
            try {
                return client.post().uri("/api/authenticate").contentType(MediaType.APPLICATION_JSON)
                        .body(new AuthRequest(props.username(), props.password(), true))
                        .retrieve().body(AuthResponse.class);
            } catch (RuntimeException e) {
                RuntimeException t = CatedraHttpSupport.translate(e);
                if (t instanceof com.integrador.turnos.application.exception.CatedraRejectedException) {
                    throw new CatedraAuthenticationException("Credenciales de la catedra invalidas", e);
                }
                throw t;
            }
        });
        if (response == null || response.idToken() == null) {
            throw new CatedraAuthenticationException("La catedra no devolvio token", null);
        }
        return response.idToken();
    }
}
