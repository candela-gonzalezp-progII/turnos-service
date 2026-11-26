package com.integrador.turnos.infrastructure.persistence.adapter;

import com.integrador.turnos.application.port.out.ProcessedEventPort;
import com.integrador.turnos.infrastructure.persistence.jpa.ProcessedEventJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ProcessedEventAdapter implements ProcessedEventPort {

    private final ProcessedEventJpaRepository jpa;

    public ProcessedEventAdapter(ProcessedEventJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public boolean register(String eventId) {
        return jpa.insertIfAbsent(eventId) == 1;
    }
}
