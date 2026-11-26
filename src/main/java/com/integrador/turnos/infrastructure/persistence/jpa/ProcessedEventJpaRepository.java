package com.integrador.turnos.infrastructure.persistence.jpa;

import com.integrador.turnos.infrastructure.persistence.entity.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventEntity, String> {

    /** Inserta solo si no existe: la clave primaria decide quien gana aunque haya dos consumidores. */
    @Modifying
    @Query(value = "INSERT INTO processed_event (event_id, processed_at) VALUES (:id, now()) ON CONFLICT (event_id) DO NOTHING",
            nativeQuery = true)
    int insertIfAbsent(@Param("id") String id);
}
