package com.integrador.turnos.infrastructure.config;

import com.integrador.turnos.application.port.in.GetAvailabilityUseCase;
import com.integrador.turnos.application.port.out.CatalogPort;
import com.integrador.turnos.application.service.AvailabilityService;
import com.integrador.turnos.infrastructure.catalogo.CatalogProperties;
import com.integrador.turnos.infrastructure.catalogo.CatalogRestAdapter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(CatalogProperties.class)
public class AppConfig {

    @Bean
    CatalogPort catalogPort(CatalogProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) props.connectTimeout().toMillis());
        factory.setReadTimeout((int) props.readTimeout().toMillis());
        RestClient client = RestClient.builder().baseUrl(props.baseUrl()).requestFactory(factory).build();
        return new CatalogRestAdapter(client);
    }

    @Bean
    GetAvailabilityUseCase getAvailabilityUseCase(CatalogPort catalogPort) {
        return new AvailabilityService(catalogPort);
    }
}
