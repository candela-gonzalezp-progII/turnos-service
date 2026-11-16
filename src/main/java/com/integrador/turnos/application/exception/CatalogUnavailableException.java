package com.integrador.turnos.application.exception;

/** catalogo-service no respondio o devolvio un error inesperado. */
public class CatalogUnavailableException extends RuntimeException {
    public CatalogUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
