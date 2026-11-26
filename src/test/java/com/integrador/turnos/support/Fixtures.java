package com.integrador.turnos.support;

import com.integrador.turnos.application.port.out.UserRepositoryPort;
import com.integrador.turnos.domain.model.ProfessionalSummary;
import com.integrador.turnos.domain.model.Reservation;
import com.integrador.turnos.domain.model.User;
import com.integrador.turnos.domain.model.WorkSchedule;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Datos de prueba compartidos. "Hoy" es 2026-11-01 y el lunes de prueba es 2026-11-23. */
public final class Fixtures {

    public static final Instant NOW = Instant.parse("2026-11-01T12:00:00Z");
    public static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    public static final LocalDate MONDAY = LocalDate.of(2026, 11, 23);
    public static final ProfessionalSummary ANA = new ProfessionalSummary(15L, "Ana Gomez", "Clinica medica", true,
            List.of(new WorkSchedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), 30, true)));

    private Fixtures() {
    }

    public static UserRepositoryPort users(User... users) {
        return new UserRepositoryPort() {
            @Override public boolean existsByLogin(String login) { return false; }
            @Override public boolean existsByEmail(String email) { return false; }
            @Override public User save(User user) { return user; }
            @Override public Optional<User> findByLogin(String login) {
                return List.of(users).stream().filter(u -> u.getLogin().equals(login)).findFirst();
            }
        };
    }

    public static User user(String login, String first, String last) {
        return new User(1L, login, "hash", first, last, login + "@mail.com", null, "es", true, Set.of("ROLE_USER"), NOW);
    }

    /** Reserva con hold creado, vence 15 minutos despues de NOW. */
    public static Reservation heldReservation(String user) {
        Reservation r = Reservation.request(user, 15L, "Ana Gomez", MONDAY, LocalTime.of(9, 0), LocalTime.of(9, 30), NOW);
        r.holdCreated("hold-1", "process-1", NOW.plusSeconds(900), NOW);
        return r;
    }
}
