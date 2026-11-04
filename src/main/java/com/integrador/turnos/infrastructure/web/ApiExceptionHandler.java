package com.integrador.turnos.infrastructure.web;

import com.integrador.turnos.application.exception.CatalogUnavailableException;
import com.integrador.turnos.application.exception.ProfessionalNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
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
    public ResponseEntity<ProblemDetail> invalid(Exception e) {
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> invalidParameter(MethodArgumentTypeMismatchException e) {
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Valor invalido para el parametro " + e.getName());
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String detail) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setTitle(status.getReasonPhrase());
        body.setProperty("code", code);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(body);
    }
}