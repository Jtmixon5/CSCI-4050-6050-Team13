package com.cinema.ebooking.service;

import com.cinema.ebooking.dto.RegisterRequest;
import com.cinema.ebooking.entity.EmailVerificationToken;
import com.cinema.ebooking.entity.User;
import com.cinema.ebooking.repository.EmailVerificationTokenRepository;
import com.cinema.ebooking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordEncryptionService passwordService;
    private final RegistrationEmailService emailService;
    private final TokenService tokenService;

    public RegistrationService(
        UserRepository userRepository,
        EmailVerificationTokenRepository tokenRepository,
        PasswordEncryptionService passwordService,
        RegistrationEmailService emailService,
        TokenService tokenService
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordService = passwordService;
        this.emailService = emailService;
        this.tokenService = tokenService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "An account with this email address already exists."
            );
        }

        User user = userRepository.save(
            new User(
                request.firstName().trim(),
                request.lastName().trim(),
                email,
                request.phoneNumber().trim(),
                passwordService.hash(request.password()),
                User.Role.CUSTOMER,
                User.AccountStatus.INACTIVE,
                request.promotionOptIn()
            )
        );

        String rawToken = tokenService.generate();
        tokenRepository.save(
            new EmailVerificationToken(
                user,
                tokenService.hash(rawToken),
                LocalDateTime.now().plusHours(24)
            )
        );

        emailService.sendConfirmation(email, rawToken);
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A verification token is required."
            );
        }

        EmailVerificationToken token = tokenRepository
            .findByTokenHash(tokenService.hash(rawToken))
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This verification link is invalid."
                )
            );

        if (token.getUsedAt() != null) {
            if (token.getUser().getAccountStatus()
                == User.AccountStatus.ACTIVE) {
                return;
            }
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "This verification link has already been used."
            );
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "This verification link has expired."
            );
        }

        token.getUser().activate();
        token.markUsed();
        tokenRepository.save(token);
        userRepository.save(token.getUser());
    }

}
