package com.dentistdss.reporting.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Quartz Scheduler Configuration
 * 
 * Configures Quartz to use the primary DataSource for job persistence
 * when multiple DataSources are present in the application.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Configuration
@Profile("!test")
public class QuartzConfig {

    /**
     * Configure Quartz to use the primary DataSource
     * The @QuartzDataSource annotation tells Spring Boot which DataSource
     * to use for Quartz when multiple DataSources are available.
     */
    @Bean
    @QuartzDataSource
    public DataSource quartzDataSource(@Qualifier("primaryDataSource") DataSource primaryDataSource) {
        log.info("Configuring Quartz to use primary DataSource");
        return primaryDataSource;
    }
}
