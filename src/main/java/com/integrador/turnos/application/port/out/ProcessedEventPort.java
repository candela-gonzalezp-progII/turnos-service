package com.integrador.turnos.application.port.out;

/** Receptor idempotente: recuerda los eventId ya procesados (tabla processed_event). */
public interface ProcessedEventPort {

    /** @return true si es la primera vez que se ve ese eventId; false si ya estaba registrado. */
    boolean register(String eventId);
}
