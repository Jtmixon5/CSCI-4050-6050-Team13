package com.cinema.ebooking.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncryptionService {

    private final PasswordEncoder passwordEncoder;

    public PasswordEncryptionService(int strength) {
        this.passwordEncoder = new BCryptPasswordEncoder(strength);
    }

    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
