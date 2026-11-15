package com.integrador.turnos.infrastructure;

import com.integrador.turnos.AbstractPostgresIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Comprueba que Flyway aplica las migraciones sobre un Postgres real. */
class SchemaIT extends AbstractPostgresIT {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void flywayCreaLasTablasDeUsuarios() {
        List<String> tables = jdbc.queryForList(
                "select table_name from information_schema.tables where table_schema = 'public'", String.class);

        assertThat(tables).contains("app_user", "app_user_authority");
    }

    @Test
    void loginYEmailSonUnicos() {
        List<String> constraints = jdbc.queryForList(
                "select constraint_name from information_schema.table_constraints where table_name = 'app_user'", String.class);

        assertThat(constraints).contains("uk_app_user_login", "uk_app_user_email");
    }
}
