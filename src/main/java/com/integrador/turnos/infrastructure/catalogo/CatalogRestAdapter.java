package com.integrador.turnos.infrastructure.catalogo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.integrador.turnos.application.exception.CatalogUnavailableException;
import com.integrador.turnos.application.exception.ProfessionalNotFoundException;
import com.integrador.turnos.application.port.out.CatalogPort;
import com.integrador.turnos.domain.model.ProfessionalSummary;
import com.integrador.turnos.domain.model.WorkSchedule;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/** Adapter: traduce GET /api/professionals/{id} de catalogo-service al modelo de turnos. */
public class CatalogRestAdapter implements CatalogPort {

    private final RestClient client;

    public CatalogRestAdapter(RestClient client) {
        this.client = client;
    }

    @Override
    public ProfessionalSummary findProfessional(long id) {
        try {
            ProfessionalDto dto = client.get()
                    .uri("/api/professionals/{id}", id)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (req, res) -> {
                        throw new ProfessionalNotFoundException(id);
                    })
                    .body(ProfessionalDto.class);
            if (dto == null) {
                throw new CatalogUnavailableException("catalogo-service devolvio una respuesta vacia", null);
            }
            return dto.toDomain();
        } catch (ProfessionalNotFoundException e) {
            throw e;
        } catch (RestClientException e) {
            throw new CatalogUnavailableException("catalogo-service no disponible", e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ProfessionalDto(Long id, String fullName, boolean enabled, CategoryDto category,
                           List<ScheduleDto> weeklySchedules) {

        ProfessionalSummary toDomain() {
            List<WorkSchedule> schedules = weeklySchedules == null ? List.of()
                    : weeklySchedules.stream().map(ScheduleDto::toDomain).toList();
            return new ProfessionalSummary(id, fullName, category == null ? null : category.name(), enabled, schedules);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CategoryDto(Long id, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ScheduleDto(Long id, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
                       int slotDurationMinutes, boolean enabled) {

        WorkSchedule toDomain() {
            return new WorkSchedule(dayOfWeek, startTime, endTime, slotDurationMinutes, enabled);
        }
    }
}
