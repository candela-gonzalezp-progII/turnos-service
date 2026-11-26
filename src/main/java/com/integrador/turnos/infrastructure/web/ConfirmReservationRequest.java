package com.integrador.turnos.infrastructure.web;

/** Nombre y apellido opcionales: si faltan se usan los de la cuenta. */
public record ConfirmReservationRequest(String patientFirstName, String patientLastName) {
}
