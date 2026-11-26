package com.integrador.turnos.infrastructure.kafka;

import org.springframework.context.SmartLifecycle;

/** Arranca y detiene el consumidor Kafka junto con la aplicacion (hilo propio, no bloquea el arranque). */
public class ReservationKafkaStarter implements SmartLifecycle {

    private final ReservationEventsConsumer consumer;
    private Thread thread;

    public ReservationKafkaStarter(ReservationEventsConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void start() {
        thread = new Thread(consumer, "reservation-events-consumer");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void stop() {
        consumer.stop();
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public boolean isRunning() {
        return thread != null && thread.isAlive();
    }
}
