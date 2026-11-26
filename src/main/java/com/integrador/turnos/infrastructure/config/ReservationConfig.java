package com.integrador.turnos.infrastructure.config;

import com.integrador.turnos.application.port.in.CancelReservationUseCase;
import com.integrador.turnos.application.port.in.ConfirmReservationUseCase;
import com.integrador.turnos.application.port.in.GetReservationUseCase;
import com.integrador.turnos.application.port.in.MyReservationsUseCase;
import com.integrador.turnos.application.port.in.StartReservationUseCase;
import com.integrador.turnos.application.port.out.AppointmentCancellationPort;
import com.integrador.turnos.application.port.out.AppointmentQueryPort;
import com.integrador.turnos.application.port.out.CatalogPort;
import com.integrador.turnos.application.port.out.HoldConfirmationPort;
import com.integrador.turnos.application.port.out.HoldPort;
import com.integrador.turnos.application.port.out.ReservationRepositoryPort;
import com.integrador.turnos.application.port.out.UserRepositoryPort;
import com.integrador.turnos.application.service.CancelReservationService;
import com.integrador.turnos.application.service.ConfirmReservationService;
import com.integrador.turnos.application.service.GetReservationService;
import com.integrador.turnos.application.service.MyReservationsService;
import com.integrador.turnos.application.service.StartReservationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ReservationConfig {

    @Bean
    StartReservationUseCase startReservationUseCase(CatalogPort catalog, HoldPort holds,
                                                    ReservationRepositoryPort reservations, Clock clock,
                                                    TurnosProperties turnos) {
        return new StartReservationService(catalog, holds, reservations, clock, ZoneId.of(turnos.zone()));
    }

    @Bean
    ConfirmReservationUseCase confirmReservationUseCase(ReservationRepositoryPort reservations,
                                                        HoldConfirmationPort confirmations, UserRepositoryPort users,
                                                        Clock clock) {
        return new ConfirmReservationService(reservations, confirmations, users, clock);
    }

    @Bean
    GetReservationUseCase getReservationUseCase(ReservationRepositoryPort reservations, Clock clock) {
        return new GetReservationService(reservations, clock);
    }

    @Bean
    MyReservationsUseCase myReservationsUseCase(ReservationRepositoryPort reservations, AppointmentQueryPort catedra,
                                                Clock clock) {
        return new MyReservationsService(reservations, catedra, clock);
    }

    @Bean
    CancelReservationUseCase cancelReservationUseCase(ReservationRepositoryPort reservations,
                                                      AppointmentCancellationPort catedra, Clock clock) {
        return new CancelReservationService(reservations, catedra, clock);
    }
}
