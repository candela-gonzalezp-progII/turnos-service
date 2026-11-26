package com.integrador.turnos.application.port.out;

import com.integrador.turnos.domain.model.AppointmentSnapshot;

import java.util.List;

/** Puerto de salida: reservas que la catedra tiene registradas para la cuenta tecnica. */
public interface AppointmentQueryPort {

    /**
     * @param externalPatientId login del usuario (se envio como externalPatientId al crear el hold)
     * @throws com.integrador.turnos.application.exception.CatedraUnavailableException si no responde
     */
    List<AppointmentSnapshot> findByPatient(String externalPatientId);
}
