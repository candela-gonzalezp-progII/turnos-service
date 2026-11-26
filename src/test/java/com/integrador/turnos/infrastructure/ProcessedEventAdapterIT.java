package com.integrador.turnos.infrastructure;

import com.integrador.turnos.AbstractPostgresIT;
import com.integrador.turnos.application.port.out.ProcessedEventPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessedEventAdapterIT extends AbstractPostgresIT {

    @Autowired
    ProcessedEventPort processed;

    @Autowired
    PlatformTransactionManager txManager;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void registraUnaVezYRechazaDuplicadoEnLaMismaTransaccion() {
        assertThat(new TransactionTemplate(txManager).<Boolean>execute(s -> processed.register("it-evt-1"))).isEqualTo(true);
        assertThat(new TransactionTemplate(txManager).<Boolean>execute(s -> processed.register("it-evt-1"))).isEqualTo(false);
    }

    @Test
    void rollbackDejaElEventoLibreParaReintentar() {
        new TransactionTemplate(txManager).execute(s -> {
            assertThat(processed.register("it-evt-2")).isTrue();
            s.setRollbackOnly();
            return null;
        });

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM processed_event WHERE event_id = ?",
                Integer.class,
                "it-evt-2"
        );
        assertThat(count).isZero();
        assertThat(new TransactionTemplate(txManager).<Boolean>execute(s -> processed.register("it-evt-2"))).isTrue();
    }

    @Test
    void registraDistintosEventosPorSeparado() {
        assertThat(new TransactionTemplate(txManager).<Boolean>execute(s -> processed.register("it-evt-3"))).isEqualTo(true);
        assertThat(new TransactionTemplate(txManager).<Boolean>execute(s -> processed.register("it-evt-4"))).isTrue();
    }
}