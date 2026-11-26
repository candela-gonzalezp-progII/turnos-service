package com.integrador.turnos.domain.model;

import java.util.List;

/**
 * Estados del proceso de reserva (maquina de estados).
 *
 * Pendientes (el proceso sigue vivo y puede expirar):
 *   HOLD_REQUESTED -> HOLD_CREATED -> CONFIRMATION_PENDING -> INFO_REQUESTED -> INFO_SUBMITTED -> CONFIRMED
 *   y, si la catedra rechaza el telefono, INFO_REJECTED (se puede reenviar hasta el vencimiento).
 * Finales: CONFIRMED (solo puede pasar a CANCELLED), CANCELLED, EXPIRED, FAILED, INVALID.
 * Un estado final no retrocede: lo que llegue tarde o duplicado se ignora.
 */
public enum ReservationStatus {
    HOLD_REQUESTED,
    HOLD_CREATED,
    CONFIRMATION_PENDING,
    INFO_REQUESTED,
    INFO_SUBMITTED,
    INFO_REJECTED,
    CONFIRMED,
    CANCELLED,
    EXPIRED,
    FAILED,
    INVALID;

    /** El proceso todavia no termino: puede avanzar, vencer o ser recuperado. */
    public boolean isPending() {
        return this.ordinal() <= INFO_REJECTED.ordinal();
    }

    public boolean isFinal() {
        return !isPending();
    }

    /** Estados que ocupan el horario del usuario (sirve para no duplicar el mismo pedido). */
    public boolean occupiesSlot() {
        return isPending() || this == CONFIRMED;
    }

    public static List<ReservationStatus> occupying() {
        return java.util.Arrays.stream(values()).filter(ReservationStatus::occupiesSlot).toList();
    }

    public static List<ReservationStatus> pending() {
        return java.util.Arrays.stream(values()).filter(ReservationStatus::isPending).toList();
    }

    /**
     * Transiciones permitidas. Son tolerantes con eventos fuera de orden (la catedra entrega "al menos una vez"
     * y REST/Kafka pueden cruzarse), pero nunca sacan a un proceso de un estado final.
     */
    public boolean canTransitionTo(ReservationStatus target) {
        return switch (target) {
            case HOLD_REQUESTED -> false;
            case HOLD_CREATED -> this == HOLD_REQUESTED;
            case CONFIRMATION_PENDING -> this == HOLD_CREATED;
            case INFO_REQUESTED -> this == HOLD_CREATED || this == CONFIRMATION_PENDING;
            case INFO_SUBMITTED -> this == INFO_REQUESTED || this == INFO_REJECTED;
            case INFO_REJECTED -> this == CONFIRMATION_PENDING || this == INFO_REQUESTED || this == INFO_SUBMITTED;
            case CONFIRMED -> isPending() && this != HOLD_REQUESTED;
            case INVALID -> isPending() && this != HOLD_REQUESTED;
            case EXPIRED, FAILED -> isPending();
            case CANCELLED -> this == CONFIRMED;
        };
    }
}
