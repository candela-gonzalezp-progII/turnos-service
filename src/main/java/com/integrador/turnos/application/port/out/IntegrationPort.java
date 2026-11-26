package com.integrador.turnos.application.port.out;

/** Puerto de salida: configuracion de la integracion que entrega la catedra (groupId, topics, consumer group). */
public interface IntegrationPort {

    Integration current();

    record Integration(String groupId, String kafkaBootstrapServers, String kafkaConsumerGroupId,
                       String actionsTopic, String phoneTopic, String provisioningStatus) {

        public boolean isProvisioned() {
            return "PROVISIONED".equals(provisioningStatus);
        }
    }
}
