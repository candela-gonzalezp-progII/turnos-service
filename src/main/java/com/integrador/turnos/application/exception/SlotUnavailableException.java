package com.integrador.turnos.application.exception;

/** El horario ya fue tomado por otra persona (SLOT_ALREADY_HELD o SLOT_ALREADY_RESERVED). */
public class SlotUnavailableException extends RuntimeException {

    private final String code;

    public SlotUnavailableException(String code) {
        super("El horario ya no esta disponible");
        this.code = code;
    }

    public String code() {
        return code;
    }
}
