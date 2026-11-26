package com.integrador.turnos.infrastructure.catedra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Solo los campos que usamos. redisPassword y demas secretos se ignoran a proposito. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record IntegrationResponse(String groupId, String kafkaBootstrapServers, String kafkaConsumerGroupId,
                                  String kafkaAppointmentActionsTopic, String kafkaAppointmentPhoneTopic,
                                  String provisioningStatus) {
}
