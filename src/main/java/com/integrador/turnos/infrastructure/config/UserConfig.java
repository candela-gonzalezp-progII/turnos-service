package com.integrador.turnos.infrastructure.config;

import com.integrador.turnos.application.port.in.RegisterUserUseCase;
import com.integrador.turnos.application.port.out.PasswordHasherPort;
import com.integrador.turnos.application.port.out.UserRepositoryPort;
import com.integrador.turnos.application.service.UserRegistrationService;
import com.integrador.turnos.infrastructure.security.BCryptPasswordHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class UserConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    PasswordHasherPort passwordHasher() {
        return new BCryptPasswordHasher();
    }

    @Bean
    RegisterUserUseCase registerUserUseCase(UserRepositoryPort users, PasswordHasherPort hasher, Clock clock) {
        return new UserRegistrationService(users, hasher, clock);
    }
}
