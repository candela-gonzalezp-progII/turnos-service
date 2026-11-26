package com.integrador.turnos.application.exception;

/** El horario pedido no existe en la agenda o ya paso (INVALID_SLOT, SLOT_IN_PAST, PROFESSIONAL_DISABLED...). */
public class InvalidSlotException extends RuntimeException {

    private final String code;

    public InvalidSlotException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
