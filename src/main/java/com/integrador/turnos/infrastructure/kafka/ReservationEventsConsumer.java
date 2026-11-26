package com.integrador.turnos.infrastructure.kafka;

import com.integrador.turnos.application.port.in.HandleReservationEventUseCase;
import com.integrador.turnos.application.port.out.IntegrationPort;
import com.integrador.turnos.domain.model.EventOutcome;
import com.integrador.turnos.domain.model.ReservationEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Consume alumnos.turnos.acciones.{groupId}. Entrega "al menos una vez":
 *  - el offset se confirma SOLO despues de procesar (commit manual);
 *  - un mensaje ilegible se descarta (reintentar no lo arregla);
 *  - un error transitorio (BD caida, conflicto) NO confirma: se vuelve a leer el mismo offset tras una espera.
 * Los duplicados los absorbe el receptor idempotente (eventId), no este consumidor.
 */
public class ReservationEventsConsumer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ReservationEventsConsumer.class);

    private final IntegrationPort integrationPort;
    private final HandleReservationEventUseCase handler;
    private final ReservationEventParser parser;
    private final String consumerGroupSuffix;
    private volatile boolean running = true;
    private volatile KafkaConsumer<String, String> consumer;

    public ReservationEventsConsumer(IntegrationPort integrationPort, HandleReservationEventUseCase handler,
                                     ReservationEventParser parser, String consumerGroupSuffix) {
        this.integrationPort = integrationPort;
        this.handler = handler;
        this.parser = parser;
        this.consumerGroupSuffix = consumerGroupSuffix;
    }

    @Override
    public void run() {
        while (running) {
            try {
                IntegrationPort.Integration integration = integrationPort.current();
                if (!integration.isProvisioned()) {
                    log.warn("Integracion de la catedra en estado {}; se reintenta en 30 s", integration.provisioningStatus());
                    pause(30_000);
                    continue;
                }
                consume(integration);
            } catch (WakeupException e) {
                return;
            } catch (RuntimeException e) {
                log.error("Consumidor Kafka caido ({}); se reintenta en 10 s", e.getClass().getSimpleName());
                pause(10_000);
            }
        }
    }

    private void consume(IntegrationPort.Integration integration) {
        Properties p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, integration.kafkaBootstrapServers());
        p.put(ConsumerConfig.GROUP_ID_CONFIG, integration.kafkaConsumerGroupId() + consumerGroupSuffix);
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        p.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "20");
        try (KafkaConsumer<String, String> c = new KafkaConsumer<>(p)) {
            consumer = c;
            c.subscribe(List.of(integration.actionsTopic()));
            log.info("Consumiendo {} con el grupo {}", integration.actionsTopic(), p.get(ConsumerConfig.GROUP_ID_CONFIG));
            while (running) {
                ConsumerRecords<String, String> records = c.poll(Duration.ofSeconds(1));
                for (ConsumerRecord<String, String> record : records) {
                    if (!process(record)) {
                        c.seek(new TopicPartition(record.topic(), record.partition()), record.offset());
                        pause(3_000);
                        break;               // vuelve a leer desde este mismo offset
                    }
                    c.commitSync();          // al terminar cada mensaje (volumen bajo: simple y seguro)
                }
            }
        } finally {
            consumer = null;
        }
    }

    /** @return false si hay que reintentar el mismo mensaje. */
    boolean process(ConsumerRecord<String, String> record) {
        Optional<ReservationEvent> parsed;
        try {
            parsed = parser.parse(record.value());
        } catch (MalformedEventException e) {
            log.warn("Mensaje descartado en {}-{}@{}: {}", record.topic(), record.partition(), record.offset(), e.getMessage());
            return true;
        }
        if (parsed.isEmpty()) {
            return true;                     // tipo o version que no usamos
        }
        try {
            EventOutcome outcome = handler.handle(parsed.get());
            log.info("Evento {} -> {}", parsed.get().eventId(), outcome);
            return true;
        } catch (RuntimeException e) {
            log.warn("Error procesando el evento {} ({}); se reintenta", parsed.get().eventId(), e.getClass().getSimpleName());
            return false;
        }
    }

    public void stop() {
        running = false;
        KafkaConsumer<String, String> c = consumer;
        if (c != null) {
            c.wakeup();
        }
    }

    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            running = false;
        }
    }
}
