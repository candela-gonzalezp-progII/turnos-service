package com.integrador.turnos.infrastructure.config;

import com.integrador.turnos.application.port.in.AuthenticateUserUseCase;
import com.integrador.turnos.application.port.in.GetAccountUseCase;
import com.integrador.turnos.application.port.out.PasswordHasherPort;
import com.integrador.turnos.application.port.out.TokenIssuerPort;
import com.integrador.turnos.application.port.out.UserRepositoryPort;
import com.integrador.turnos.application.service.AccountService;
import com.integrador.turnos.application.service.AuthenticationService;
import com.integrador.turnos.infrastructure.security.AccessPolicy;
import com.integrador.turnos.infrastructure.security.HmacJwtService;
import com.integrador.turnos.infrastructure.security.JwtAuthenticationFilter;
import com.integrador.turnos.infrastructure.security.JwtProperties;
import com.integrador.turnos.infrastructure.security.JwtTokenIssuer;
import com.integrador.turnos.infrastructure.security.ProblemJsonWriter;
import com.integrador.turnos.infrastructure.security.ServiceTokenProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    /** Falla al arrancar si JWT_SECRET falta o es corto (menos de 32 caracteres). */
    @Bean
    HmacJwtService hmacJwtService(JwtProperties props, Clock clock) {
        return new HmacJwtService(props.secret(), props.issuer(), clock);
    }

    @Bean
    ServiceTokenProvider serviceTokenProvider(HmacJwtService jwt, JwtProperties props) {
        return new ServiceTokenProvider(jwt, props.serviceTokenTtl());
    }

    @Bean
    TokenIssuerPort tokenIssuer(HmacJwtService jwt, JwtProperties props) {
        return new JwtTokenIssuer(jwt, props.expiration());
    }

    @Bean
    AuthenticateUserUseCase authenticateUserUseCase(UserRepositoryPort users, PasswordHasherPort hasher, TokenIssuerPort tokens) {
        return new AuthenticationService(users, hasher, tokens);
    }

    @Bean
    GetAccountUseCase getAccountUseCase(UserRepositoryPort users) {
        return new AccountService(users);
    }

    @Bean
    FilterRegistrationBean<JwtAuthenticationFilter> jwtFilter(HmacJwtService jwt) {
        FilterRegistrationBean<JwtAuthenticationFilter> reg =
                new FilterRegistrationBean<>(new JwtAuthenticationFilter(jwt, new AccessPolicy(), new ProblemJsonWriter()));
        reg.addUrlPatterns("/api/*");
        reg.setOrder(1);
        return reg;
    }
}