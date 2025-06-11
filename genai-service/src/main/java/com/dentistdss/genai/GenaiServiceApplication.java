package com.dentistdss.genai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@SpringBootApplication
@EnableReactiveMongoRepositories
public class GenaiServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GenaiServiceApplication.class, args);
    }
}