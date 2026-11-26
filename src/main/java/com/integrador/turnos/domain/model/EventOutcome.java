package com.integrador.turnos.domain.model;

/** Resultado de procesar un evento de la catedra. Todos permiten confirmar el offset salvo un error. */
public enum EventOutcome {
    /** Cambio el estado de la reserva. */
    APPLIED,
    /** El eventId ya se habia procesado: no se repiten efectos. */
    DUPLICATE,
    /** Llego tarde o fuera de orden y no corresponde al estado actual (un estado final no retrocede). */
    IGNORED,
    /** No hay reserva local con ese reservationProcessId (no se registra: puede llegar al reintentar). */
    UNKNOWN_PROCESS
}
