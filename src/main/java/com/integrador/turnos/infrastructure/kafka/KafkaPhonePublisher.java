package com.integrador.turnos.infrastructure.kafka;

import com.integrador.turnos.application.exception.PhonePublishException;
import com.integrador.turnos.application.port.out.IntegrationPort;
import com.integrador.turnos.application.port.out.PhonePublisherPort;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Publica AdditionalInformationSubmitted. Key = reservationProcessId (conserva el orden por proceso).
 * acks=all + idempotencia del productor; el envio se espera (maximo 10 s) para poder informar el error al usuario.
 */
public class KafkaPhonePublisher implements PhonePublisherPort, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(KafkaPhonePublisher.class);

    private final IntegrationPort integrationPort;
    private final JsonMapper mapper = JsonMapper.builder().build();
    private volatile KafkaProducer<String, String> producer;

    public KafkaPhonePublisher(IntegrationPort integrationPort) {
        this.integrationPort = integrationPort;
    }

    @Override
    public void publish(PhoneSubmission s) {
        try {
            IntegrationPort.Integration integration = integrationPort.current();
            if (!integration.isProvisioned()) {
                throw new PhonePublishException("La integracion Kafka todavia no esta provisionada", null);
            }
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("eventId", s.eventId());
            body.put("eventType", "AdditionalInformationSubmitted");
            body.put("groupId", integration.groupId());
            body.put("reservationProcessId", s.reservationProcessId());
            body.put("holdId", s.holdId());
            body.put("requestEventId", s.requestEventId());
            body.put("information", Map.of("phoneNumber", s.phoneNumber()));
            body.put("occurredAt", Instant.now().toString());
            body.put("schemaVersion", 1);
            producer(integration).send(new ProducerRecord<>(integration.phoneTopic(), s.reservationProcessId(),
                    mapper.writeValueAsString(body))).get(10, TimeUnit.SECONDS);
            log.info("Telefono publicado para el proceso {} (eventId={})", s.reservationProcessId(), s.eventId());
        } catch (PhonePublishException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PhonePublishException("Envio interrumpido", e);
        } catch (Exception e) {
            throw new PhonePublishException("No se pudo publicar el telefono en Kafka", e);
        }
    }

    private KafkaProducer<String, String> producer(IntegrationPort.Integration integration) {
        KafkaProducer<String, String> current = producer;
        if (current == null) {
            synchronized (this) {
                if (producer == null) {
                    Properties p = new Properties();
                    p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, integration.kafkaBootstrapServers());
                    p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                    p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                    p.put(ProducerConfig.ACKS_CONFIG, "all");
                    p.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
                    p.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "15000");
                    p.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "8000");
                    p.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "8000");
                    producer = new KafkaProducer<>(p);
                }
                current = producer;
            }
        }
        return current;
    }

    @Override
    public void close() {
        KafkaProducer<String, String> p = producer;
        if (p != null) {
            p.close(java.time.Duration.ofSeconds(3));
        }
    }
}
