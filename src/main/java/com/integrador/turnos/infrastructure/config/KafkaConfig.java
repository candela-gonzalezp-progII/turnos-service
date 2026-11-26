package com.integrador.turnos.infrastructure.config;

import com.integrador.turnos.application.port.in.HandleReservationEventUseCase;
import com.integrador.turnos.application.port.in.SubmitPhoneUseCase;
import com.integrador.turnos.application.port.out.IntegrationPort;
import com.integrador.turnos.application.port.out.PhonePublisherPort;
import com.integrador.turnos.application.port.out.ProcessedEventPort;
import com.integrador.turnos.application.port.out.ReservationRepositoryPort;
import com.integrador.turnos.application.service.ReservationEventService;
import com.integrador.turnos.application.service.SubmitPhoneService;
import com.integrador.turnos.infrastructure.catedra.CatedraHttp;
import com.integrador.turnos.infrastructure.catedra.CatedraIntegrationProvider;
import com.integrador.turnos.infrastructure.catedra.CatedraProperties;
import com.integrador.turnos.infrastructure.kafka.KafkaPhonePublisher;
import com.integrador.turnos.infrastructure.kafka.ReservationEventParser;
import com.integrador.turnos.infrastructure.kafka.ReservationEventsConsumer;
import com.integrador.turnos.infrastructure.kafka.ReservationKafkaStarter;
import com.integrador.turnos.infrastructure.kafka.TransactionalReservationEventHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;

/**
 * Integracion Kafka de la catedra. El consumidor solo arranca con CATEDRA_KAFKA_ENABLED=true (asi los tests y el
 * desarrollo sin cuenta tecnica no intentan conectarse).
 */
@Configuration
public class KafkaConfig {

    @Bean
    IntegrationPort integrationPort(CatedraHttp http) {
        return new CatedraIntegrationProvider(http);
    }

    @Bean(destroyMethod = "close")
    KafkaPhonePublisher kafkaPhonePublisher(IntegrationPort integrationPort) {
        return new KafkaPhonePublisher(integrationPort);
    }

    @Bean
    SubmitPhoneUseCase submitPhoneUseCase(ReservationRepositoryPort reservations, PhonePublisherPort publisher, Clock clock) {
        return new SubmitPhoneService(reservations, publisher, clock);
    }

    @Bean
    HandleReservationEventUseCase handleReservationEventUseCase(ReservationRepositoryPort reservations,
                                                                ProcessedEventPort processed, Clock clock,
                                                                PlatformTransactionManager txManager) {
        return new TransactionalReservationEventHandler(new ReservationEventService(reservations, processed, clock),
                new TransactionTemplate(txManager));
    }

    @Bean
    @ConditionalOnProperty(name = "catedra.kafka-enabled", havingValue = "true")
    ReservationKafkaStarter reservationKafkaStarter(IntegrationPort integrationPort,
                                                    HandleReservationEventUseCase handler, CatedraProperties props) {
        return new ReservationKafkaStarter(new ReservationEventsConsumer(integrationPort, handler,
                new ReservationEventParser(), props.kafkaConsumerGroupSuffix()));
    }
}
