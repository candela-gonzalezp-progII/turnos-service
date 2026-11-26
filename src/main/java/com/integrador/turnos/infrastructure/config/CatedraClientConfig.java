package com.integrador.turnos.infrastructure.config;

import com.integrador.turnos.infrastructure.catedra.CatedraHttp;
import com.integrador.turnos.infrastructure.catedra.CatedraProperties;
import com.integrador.turnos.infrastructure.catedra.CatedraReservationAdapter;
import com.integrador.turnos.infrastructure.catedra.CatedraTokenProvider;
import com.integrador.turnos.infrastructure.catedra.RetryExecutor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** Cliente REST de la catedra. La URL y las credenciales vienen del .env (CATEDRA_*). */
@Configuration
@EnableConfigurationProperties({CatedraProperties.class, TurnosProperties.class})
public class CatedraClientConfig {

    @Bean
    RestClient catedraRestClient(CatedraProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) props.connectTimeout().toMillis());
        factory.setReadTimeout((int) props.readTimeout().toMillis());
        RestClient.Builder builder = RestClient.builder().requestFactory(factory);
        if (props.baseUrl() != null && !props.baseUrl().isBlank()) {
            builder.baseUrl(props.baseUrl());
        }
        return builder.build();
    }

    @Bean
    RetryExecutor catedraRetryExecutor(CatedraProperties props) {
        return new RetryExecutor(props.maxAttempts(), props.retryBackoff());
    }

    @Bean
    CatedraTokenProvider catedraTokenProvider(RestClient catedraRestClient, CatedraProperties props,
                                              RetryExecutor catedraRetryExecutor) {
        return new CatedraTokenProvider(catedraRestClient, props, catedraRetryExecutor);
    }

    @Bean
    CatedraHttp catedraHttp(RestClient catedraRestClient, CatedraTokenProvider tokens, RetryExecutor catedraRetryExecutor) {
        return new CatedraHttp(catedraRestClient, tokens, catedraRetryExecutor);
    }

    @Bean
    CatedraReservationAdapter catedraReservationAdapter(CatedraHttp http) {
        return new CatedraReservationAdapter(http);
    }
}
