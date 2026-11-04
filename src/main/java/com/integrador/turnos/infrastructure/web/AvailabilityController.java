package com.integrador.turnos.infrastructure.web;

import com.integrador.turnos.application.port.in.GetAvailabilityUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/professionals")
public class AvailabilityController {

    private final GetAvailabilityUseCase useCase;

    public AvailabilityController(GetAvailabilityUseCase useCase) {
        this.useCase = useCase;
    }

    /** GET /api/professionals/{id}/availability?date=2026-11-02 */
    @GetMapping("/{id}/availability")
    public AvailabilityResponse availability(@PathVariable long id,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return AvailabilityResponse.from(useCase.getAvailability(id, date));
    }
}
