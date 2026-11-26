package com.integrador.turnos.infrastructure.web;

import com.integrador.turnos.application.exception.ConcurrentReservationUpdateException;
import com.integrador.turnos.application.exception.HoldExpiredException;
import com.integrador.turnos.application.exception.InvalidSlotException;
import com.integrador.turnos.application.exception.ReservationNotFoundException;
import com.integrador.turnos.application.exception.SlotUnavailableException;
import com.integrador.turnos.domain.exception.InvalidReservationStateException;
import com.integrador.turnos.application.exception.CatalogUnavailableException;
import com.integrador.turnos.application.exception.CatedraRejectedException;
import com.integrador.turnos.application.exception.CatedraUnavailableException;
import com.integrador.turnos.application.exception.PhonePublishException;
import com.integrador.turnos.application.exception.ProfessionalDisabledException;
import com.integrador.turnos.application.exception.InvalidCredentialsException;
import com.integrador.turnos.application.exception.ProfessionalNotFoundException;
import com.integrador.turnos.application.exception.UserNotFoundException;
import com.integrador.turnos.application.exception.UserAlreadyExistsException;
import com.integrador.turnos.domain.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Errores como application/problem+json con "code" estable (mismo formato que catalogo y la catedra). */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ProfessionalNotFoundException.class)
    public ResponseEntity<ProblemDetail> notFound(ProfessionalNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "PROFESSIONAL_NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(CatalogUnavailableException.class)
    public ResponseEntity<ProblemDetail> catalogDown(CatalogUnavailableException e) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "CATALOG_UNAVAILABLE", "El catalogo no esta disponible, reintente mas tarde");
    }

    @ExceptionHandler({IllegalArgumentException.class, MissingServletRequestParameterException.class})
    ResponseEntity<ProblemDetail> invalid(Exception e) {
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ProblemDetail> invalidParameter(MethodArgumentTypeMismatchException e) {
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Valor invalido para el parametro " + e.getName());
    }

    @ExceptionHandler(ValidationException.class)
    ResponseEntity<ProblemDetail> validation(ValidationException e) {
        ResponseEntity<ProblemDetail> r = problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Hay datos invalidos");
        r.getBody().setProperty("errors", e.errors());
        return r;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> unreadable(HttpMessageNotReadableException e) {
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "El cuerpo de la peticion no es un JSON valido");
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    ResponseEntity<ProblemDetail> alreadyExists(UserAlreadyExistsException e) {
        return problem(HttpStatus.CONFLICT, e.field().equals("email") ? "EMAIL_ALREADY_USED" : "LOGIN_ALREADY_USED", e.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ProblemDetail> badCredentials(InvalidCredentialsException e) {
        return problem(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Usuario o clave incorrectos");
    }

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<ProblemDetail> userGone(UserNotFoundException e) {
        return problem(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "La cuenta ya no existe");
    }

    @ExceptionHandler(ProfessionalDisabledException.class)
    ResponseEntity<ProblemDetail> professionalDisabled(ProfessionalDisabledException e) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "PROFESSIONAL_DISABLED", e.getMessage());
    }

    @ExceptionHandler(CatedraUnavailableException.class)
    ResponseEntity<ProblemDetail> catedraDown(CatedraUnavailableException e) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "CATEDRA_UNAVAILABLE", "La catedra no esta disponible, reintente mas tarde");
    }

    @ExceptionHandler(CatedraRejectedException.class)
    ResponseEntity<ProblemDetail> catedraRejected(CatedraRejectedException e) {
        return problem(HttpStatus.BAD_GATEWAY, "CATEDRA_ERROR", "La catedra rechazo la operacion");
    }

    @ExceptionHandler(SlotUnavailableException.class)
    ResponseEntity<ProblemDetail> slotTaken(SlotUnavailableException e) {
        return problem(HttpStatus.CONFLICT, e.code(), e.getMessage());
    }

    @ExceptionHandler(InvalidSlotException.class)
    ResponseEntity<ProblemDetail> invalidSlot(InvalidSlotException e) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, e.code(), e.getMessage());
    }

    @ExceptionHandler(HoldExpiredException.class)
    ResponseEntity<ProblemDetail> holdExpired(HoldExpiredException e) {
        return problem(HttpStatus.GONE, "HOLD_EXPIRED", e.getMessage());
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    ResponseEntity<ProblemDetail> reservationNotFound(ReservationNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "No existe la reserva");
    }

    @ExceptionHandler(InvalidReservationStateException.class)
    ResponseEntity<ProblemDetail> invalidState(InvalidReservationStateException e) {
        return problem(HttpStatus.CONFLICT, "INVALID_RESERVATION_STATE", e.getMessage());
    }

    @ExceptionHandler(ConcurrentReservationUpdateException.class)
    ResponseEntity<ProblemDetail> concurrent(ConcurrentReservationUpdateException e) {
        return problem(HttpStatus.CONFLICT, "CONCURRENT_UPDATE", "La reserva cambio en simultaneo, reintente");
    }

    @ExceptionHandler(PhonePublishException.class)
    ResponseEntity<ProblemDetail> phonePublish(PhonePublishException e) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "KAFKA_UNAVAILABLE", "No se pudo enviar el telefono, reintente");
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String detail) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setTitle(status.getReasonPhrase());
        body.setProperty("code", code);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(body);
    }
}
