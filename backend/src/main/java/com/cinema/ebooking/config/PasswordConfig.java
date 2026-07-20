package com.cinema.ebooking.config;

import com.cinema.ebooking.service.PasswordEncryptionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PasswordConfig {

    @Bean
    PasswordEncryptionService passwordEncryptionService() {
        return new PasswordEncryptionService(12);
    }
}
