package com.integrador.turnos.infrastructure.catedra;

import com.integrador.turnos.application.exception.CatedraAuthenticationException;
import com.integrador.turnos.application.exception.CatedraRejectedException;
import com.integrador.turnos.application.exception.CatedraUnavailableException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

/** Traduce los errores de RestClient a las excepciones de la aplicacion. */
final class CatedraHttpSupport {

    private CatedraHttpSupport() {
    }

    static RuntimeException translate(RuntimeException e) {
        if (e instanceof CatedraAuthenticationException || e instanceof CatedraUnavailableException
                || e instanceof CatedraRejectedException) {
            return e;
        }
        if (e instanceof RestClientResponseException r) {
            int status = r.getStatusCode().value();
            String code = ProblemCodeExtractor.extract(r.getResponseBodyAsString());
            if (status == 401 || (status == 403 && code == null)) {
                return new CatedraAuthenticationException("La catedra rechazo el token (HTTP " + status + ")", e);
            }
            if (status >= 500 || status == 408 || status == 429) {
                return new CatedraUnavailableException("La catedra respondio HTTP " + status, e);
            }
            return new CatedraRejectedException("La catedra rechazo la operacion (HTTP " + status + ", " + code + ")",
                    status, code, e);
        }
        if (e instanceof ResourceAccessException) {
            return new CatedraUnavailableException("No se pudo contactar a la catedra", e);
        }
        return e;
    }
}
