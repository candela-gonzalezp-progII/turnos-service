package com.integrador.turnos.support;

import com.integrador.turnos.application.port.out.ProcessedEventPort;

import java.util.HashSet;
import java.util.Set;

public class FakeProcessedEvents implements ProcessedEventPort {

    public final Set<String> seen = new HashSet<>();

    @Override
    public boolean register(String eventId) {
        return seen.add(eventId);
    }
}
