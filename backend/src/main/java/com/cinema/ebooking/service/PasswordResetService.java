package com.cinema.ebooking.service;

import com.cinema.ebooking.dto.ResetPasswordRequest;
import com.cinema.ebooking.entity.PasswordResetToken;
import com.cinema.ebooking.entity.User;
import com.cinema.ebooking.repository.PasswordResetTokenRepository;
import com.cinema.ebooking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class PasswordResetService {
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final TokenService tokenService;
    private final PasswordEncryptionService passwordService;
    private final RegistrationEmailService emailService;

    public PasswordResetService(
        UserRepository userRepository,
        PasswordResetTokenRepository tokenRepository,
        TokenService tokenService,
        PasswordEncryptionService passwordService,
        RegistrationEmailService emailService
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.tokenService = tokenService;
        this.passwordService = passwordService;
        this.emailService = emailService;
    }

    @Transactional
    public void requestReset(String emailValue) {
        String email = emailValue.trim().toLowerCase(Locale.ROOT);
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            String rawToken = tokenService.generate();
            tokenRepository.save(
                new PasswordResetToken(
                    user,
                    tokenService.hash(rawToken),
                    LocalDateTime.now().plusHours(1)
                )
            );
            emailService.sendPasswordReset(user.getEmail(), rawToken);
        });
    }

    @Transactional
    public void reset(ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepository
            .findByTokenHash(tokenService.hash(request.token()))
            .orElseThrow(this::invalidToken);

        if (token.getUsedAt() != null
            || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw invalidToken();
        }

        User user = token.getUser();
        user.changePassword(passwordService.hash(request.password()));
        token.markUsed();
        userRepository.save(user);
        tokenRepository.save(token);
    }

    private ResponseStatusException invalidToken() {
        return new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "This password reset link is invalid or expired."
        );
    }
}
