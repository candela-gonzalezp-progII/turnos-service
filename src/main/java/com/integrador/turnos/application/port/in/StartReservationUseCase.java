package com.integrador.turnos.application.port.in;

import com.integrador.turnos.domain.model.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;

/** CU-10: solicitar un bloqueo temporal (hold) para un horario. */
public interface StartReservationUseCase {

    Reservation start(String userLogin, long professionalId, LocalDate date, LocalTime startTime);
}
