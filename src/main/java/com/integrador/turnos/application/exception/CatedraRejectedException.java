package com.integrador.turnos.application.exception;

/** La catedra respondio con un rechazo de negocio (4xx). Lleva el code estable del problem+json. */
public class CatedraRejectedException extends RuntimeException {

    private final int httpStatus;
    private final String code;

    public CatedraRejectedException(String message, int httpStatus, String code, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public int httpStatus() {
        return httpStatus;
    }

    public String code() {
        return code;
    }

    public boolean hasCode(String expected) {
        return expected.equals(code);
    }
}
