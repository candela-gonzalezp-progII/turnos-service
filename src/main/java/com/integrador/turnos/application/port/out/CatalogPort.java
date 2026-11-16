package com.integrador.turnos.application.port.out;

import com.integrador.turnos.domain.model.ProfessionalSummary;

/** Puerto de salida hacia catalogo-service (hoy REST). */
public interface CatalogPort {

    /** @throws com.integrador.turnos.application.exception.ProfessionalNotFoundException si no existe
     *  @throws com.integrador.turnos.application.exception.CatalogUnavailableException si catalogo no responde */
    ProfessionalSummary findProfessional(long id);
}
