package com.integrador.turnos.infrastructure.catedra;

import com.integrador.turnos.application.port.out.IntegrationPort;
import com.integrador.turnos.infrastructure.catedra.dto.IntegrationResponse;

/** GET /api/student/integration, con cache en memoria (los topics no cambian mientras la app corre). */
public class CatedraIntegrationProvider implements IntegrationPort {

    private final CatedraHttp http;
    private volatile Integration cached;

    public CatedraIntegrationProvider(CatedraHttp http) {
        this.http = http;
    }

    @Override
    public Integration current() {
        Integration value = cached;
        if (value != null) {
            return value;
        }
        IntegrationResponse r = http.get("/api/student/integration", IntegrationResponse.class);
        Integration loaded = new Integration(r.groupId(), r.kafkaBootstrapServers(), r.kafkaConsumerGroupId(),
                r.kafkaAppointmentActionsTopic(), r.kafkaAppointmentPhoneTopic(), r.provisioningStatus());
        if (loaded.isProvisioned()) {
            cached = loaded;       // solo se cachea cuando esta lista
        }
        return loaded;
    }
}
