package com.integrador.turnos.infrastructure.config;

import com.integrador.turnos.application.port.in.GetAvailabilityUseCase;
import com.integrador.turnos.application.port.out.CatalogPort;
import com.integrador.turnos.application.port.out.OccupancyPort;
import com.integrador.turnos.application.service.AvailabilityService;
import com.integrador.turnos.infrastructure.catalogo.CatalogProperties;
import com.integrador.turnos.infrastructure.catalogo.CatalogRestAdapter;
import com.integrador.turnos.infrastructure.security.ServiceTokenProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
@EnableConfigurationProperties(CatalogProperties.class)
public class AppConfig {

    /** Cada llamada al catalogo lleva un token de servicio (ROLE_SERVICE, vida corta). */
    @Bean
    CatalogPort catalogPort(CatalogProperties props, ServiceTokenProvider serviceTokens) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) props.connectTimeout().toMillis());
        factory.setReadTimeout((int) props.readTimeout().toMillis());
        RestClient client = RestClient.builder()
                .baseUrl(props.baseUrl())
                .requestFactory(factory)
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().setBearerAuth(serviceTokens.token());
                    return execution.execute(request, body);
                })
                .build();
        return new CatalogRestAdapter(client);
    }

    @Bean
    GetAvailabilityUseCase getAvailabilityUseCase(CatalogPort catalogPort, OccupancyPort occupancyPort, Clock clock,
                                                  TurnosProperties turnos) {
        return new AvailabilityService(catalogPort, occupancyPort, clock, ZoneId.of(turnos.zone()));
    }
}
