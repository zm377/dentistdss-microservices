package com.dentistdss.reporting.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;


/**
 * Database Configuration for Reporting Service
 * 
 * Configures dual data sources:
 * - Primary: For write operations and metadata
 * - Replica: For read-only analytical queries (performance optimization)
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Configuration
@Profile("!test")
public class DatabaseConfig {

    @Value("${spring.datasource.primary.url}")
    private String primaryUrl;

    @Value("${spring.datasource.primary.username}")
    private String primaryUsername;

    @Value("${spring.datasource.primary.password}")
    private String primaryPassword;

    @Value("${spring.datasource.primary.driver-class-name:org.postgresql.Driver}")
    private String primaryDriverClassName;

    @Value("${spring.datasource.replica.url}")
    private String replicaUrl;

    @Value("${spring.datasource.replica.username}")
    private String replicaUsername;

    @Value("${spring.datasource.replica.password}")
    private String replicaPassword;

    @Value("${spring.datasource.replica.driver-class-name:org.postgresql.Driver}")
    private String replicaDriverClassName;

    @Primary
    @Bean(name = "primaryDataSource")
    public DataSource primaryDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(primaryUrl);
        config.setUsername(primaryUsername);
        config.setPassword(primaryPassword);
        config.setDriverClassName(primaryDriverClassName);

        // Performance optimizations for primary datasource
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setPoolName("ReportingPrimaryHikariPool");

        // Connection validation
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);

        log.info("Configured primary datasource with pool size: {}", config.getMaximumPoolSize());
        return new HikariDataSource(config);
    }

    @Bean(name = "replicaDataSource")
    public DataSource replicaDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(replicaUrl);
        config.setUsername(replicaUsername);
        config.setPassword(replicaPassword);
        config.setDriverClassName(replicaDriverClassName);

        // Performance optimizations for read replica
        config.setMaximumPoolSize(15);
        config.setMinimumIdle(3);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setPoolName("ReportingReplicaHikariPool");
        config.setReadOnly(true); // Optimize for read-only operations

        // Connection validation
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);

        log.info("Configured replica datasource with pool size: {}", config.getMaximumPoolSize());
        return new HikariDataSource(config);
    }



    @Bean(name = "primaryJdbcTemplate")
    public JdbcTemplate primaryJdbcTemplate(@Qualifier("primaryDataSource") DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setQueryTimeout(300); // 5 minutes timeout for complex queries
        return jdbcTemplate;
    }

    @Bean(name = "replicaJdbcTemplate")
    public JdbcTemplate replicaJdbcTemplate(@Qualifier("replicaDataSource") DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setQueryTimeout(300); // 5 minutes timeout for analytical queries
        jdbcTemplate.setFetchSize(1000); // Optimize for large result sets
        return jdbcTemplate;
    }

}
