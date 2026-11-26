package com.integrador.turnos.application.port.in;

import com.integrador.turnos.domain.model.Reservation;

/** CU-12: enviar el telefono que la catedra pidio por Kafka. */
public interface SubmitPhoneUseCase {

    Reservation submit(String userLogin, String reservationId, String phoneNumber);
}
