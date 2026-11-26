package com.integrador.turnos.infrastructure.web;

import com.integrador.turnos.application.port.in.ConfirmReservationUseCase;
import com.integrador.turnos.application.port.in.GetReservationUseCase;
import com.integrador.turnos.application.port.in.StartReservationUseCase;
import com.integrador.turnos.application.port.in.SubmitPhoneUseCase;
import com.integrador.turnos.infrastructure.security.AuthAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Reservas del usuario autenticado. El usuario sale del token, nunca del cuerpo ni de la URL. */
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final StartReservationUseCase start;
    private final ConfirmReservationUseCase confirm;
    private final GetReservationUseCase get;
    private final SubmitPhoneUseCase submitPhone;

    public ReservationController(StartReservationUseCase start, ConfirmReservationUseCase confirm,
                                 GetReservationUseCase get, SubmitPhoneUseCase submitPhone) {
        this.start = start;
        this.confirm = confirm;
        this.get = get;
        this.submitPhone = submitPhone;
    }

    /** CU-10. 201: proceso creado con el horario bloqueado. */
    @PostMapping
    public ResponseEntity<ReservationResponse> start(@RequestAttribute(AuthAttributes.LOGIN) String login,
                                                     @RequestBody StartReservationRequest body) {
        if (body == null || body.professionalId() == null || body.date() == null || body.startTime() == null) {
            throw new IllegalArgumentException("professionalId, date y startTime son obligatorios");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservationResponse.from(
                start.start(login, body.professionalId(), body.date(), body.startTime())));
    }

    /** CU-11. 202: la catedra va a pedir el telefono por Kafka; la app consulta el estado. */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<ReservationResponse> confirm(@RequestAttribute(AuthAttributes.LOGIN) String login,
                                                       @PathVariable String id,
                                                       @RequestBody(required = false) ConfirmReservationRequest body) {
        String first = body == null ? null : body.patientFirstName();
        String last = body == null ? null : body.patientLastName();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ReservationResponse.from(
                confirm.confirm(login, id, first, last)));
    }

    /** CU-12. 202: el telefono se publico en Kafka; el resultado llega por evento y se ve consultando el estado. */
    @PostMapping("/{id}/phone")
    public ResponseEntity<ReservationResponse> phone(@RequestAttribute(AuthAttributes.LOGIN) String login,
                                                     @PathVariable String id, @RequestBody SubmitPhoneRequest body) {
        if (body == null) {
            throw new IllegalArgumentException("phoneNumber es obligatorio");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ReservationResponse.from(
                submitPhone.submit(login, id, body.phoneNumber())));
    }

    @GetMapping("/{id}")
    public ReservationResponse get(@RequestAttribute(AuthAttributes.LOGIN) String login, @PathVariable String id) {
        return ReservationResponse.from(this.get.get(login, id));
    }
}
