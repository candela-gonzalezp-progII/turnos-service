package com.integrador.turnos.application.exception;

/** No se pudo publicar el telefono en Kafka (el estado local no cambia; el usuario puede reintentar). */
public class PhonePublishException extends RuntimeException {
    public PhonePublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
