package com.integrador.turnos.infrastructure.web;

import com.integrador.turnos.application.port.in.MyReservationsUseCase;

import java.util.List;

/** verified=false: la catedra no respondio y los estados son los ultimos conocidos localmente. */
public record MyReservationsResponse(boolean verified, List<ReservationResponse> reservations) {

    public static MyReservationsResponse from(MyReservationsUseCase.MyReservations result) {
        return new MyReservationsResponse(result.verified(),
                result.reservations().stream().map(ReservationResponse::from).toList());
    }
}
