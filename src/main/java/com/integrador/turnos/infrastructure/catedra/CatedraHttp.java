package com.integrador.turnos.infrastructure.catedra;

import com.integrador.turnos.application.exception.CatedraAuthenticationException;
import com.integrador.turnos.application.exception.CatedraRejectedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.function.Supplier;

/**
 * Cliente autenticado con el JWT tecnico de la catedra.
 *  - GET: reintentos acotados con backoff ante errores transitorios.
 *  - POST: UN solo intento (un timeout no prueba que fallo; se reconcilia por estado, nunca se repite a ciegas).
 *  - Ante 401/403 sin code se vuelve a autenticar UNA sola vez.
 */
public class CatedraHttp {

    private final RestClient restClient;
    private final CatedraTokenProvider tokens;
    private final RetryExecutor retry;

    public CatedraHttp(RestClient restClient, CatedraTokenProvider tokens, RetryExecutor retry) {
        this.restClient = restClient;
        this.tokens = tokens;
        this.retry = retry;
    }

    public <T> T get(String uri, Class<T> type, Object... uriVariables) {
        return withReauth(() -> retry.execute(() -> doGet(uri, type, uriVariables)));
    }

    public <T> T post(String uri, Object body, Class<T> type, Object... uriVariables) {
        return withReauth(() -> doPost(uri, body, type, uriVariables));
    }

    private <T> T withReauth(Supplier<T> call) {
        try {
            return call.get();
        } catch (CatedraAuthenticationException e) {
            tokens.invalidate();
            return call.get();
        }
    }

    private <T> T doGet(String uri, Class<T> type, Object... uriVariables) {
        try {
            T body = restClient.get().uri(uri, uriVariables)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.bearerToken())
                    .retrieve().body(type);
            return requireBody(uri, body);
        } catch (RuntimeException e) {
            throw CatedraHttpSupport.translate(e);
        }
    }

    private <T> T doPost(String uri, Object body, Class<T> type, Object... uriVariables) {
        try {
            T response = restClient.post().uri(uri, uriVariables)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.bearerToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body == null ? java.util.Map.of() : body)
                    .retrieve().body(type);
            return requireBody(uri, response);
        } catch (RuntimeException e) {
            throw CatedraHttpSupport.translate(e);
        }
    }

    private static <T> T requireBody(String uri, T body) {
        if (body == null) {
            throw new CatedraRejectedException("La catedra devolvio una respuesta vacia en " + uri, 0, null, null);
        }
        return body;
    }
}
