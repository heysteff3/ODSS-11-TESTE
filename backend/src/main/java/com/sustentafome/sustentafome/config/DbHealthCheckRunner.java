package com.sustentafome.sustentafome.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile({"prod","stage","azure"})
public class DbHealthCheckRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DbHealthCheckRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        String dbName = jdbcTemplate.queryForObject("SELECT DB_NAME()", String.class);
        String server = jdbcTemplate.queryForObject("SELECT @@SERVERNAME", String.class);
        System.out.println("✅ Conectado no banco: " + dbName + " | Servidor: " + server);
    }
}
