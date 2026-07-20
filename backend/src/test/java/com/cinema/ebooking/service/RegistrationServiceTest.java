package com.cinema.ebooking.service;

import com.cinema.ebooking.dto.RegisterRequest;
import com.cinema.ebooking.entity.EmailVerificationToken;
import com.cinema.ebooking.entity.User;
import com.cinema.ebooking.repository.EmailVerificationTokenRepository;
import com.cinema.ebooking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrationServiceTest {
    private UserRepository userRepository;
    private EmailVerificationTokenRepository tokenRepository;
    private PasswordEncryptionService passwordService;
    private RegistrationEmailService emailService;
    private TokenService tokenService;
    private RegistrationService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        tokenRepository = mock(EmailVerificationTokenRepository.class);
        passwordService = mock(PasswordEncryptionService.class);
        emailService = mock(RegistrationEmailService.class);
        tokenService = mock(TokenService.class);
        service = new RegistrationService(
            userRepository,
            tokenRepository,
            passwordService,
            emailService,
            tokenService
        );
    }

    @Test
    void registrationStoresInactiveCustomerWithHashedPasswordAndToken() {
        RegisterRequest request = new RegisterRequest(
            "Ada",
            "Lovelace",
            "ADA@Example.com",
            "706-555-0100",
            "Secure1!",
            true
        );
        when(passwordService.hash("Secure1!")).thenReturn("bcrypt-hash");
        when(tokenService.generate()).thenReturn("raw-secret-token");
        when(tokenService.hash("raw-secret-token")).thenReturn("token-hash");
        when(userRepository.save(any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        service.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User user = userCaptor.getValue();
        assertEquals("ada@example.com", user.getEmail());
        assertEquals("bcrypt-hash", user.getPasswordHash());
        assertNotEquals("Secure1!", user.getPasswordHash());
        assertEquals(User.Role.CUSTOMER, user.getRole());
        assertEquals(User.AccountStatus.INACTIVE, user.getAccountStatus());

        ArgumentCaptor<EmailVerificationToken> tokenCaptor =
            ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        assertNotNull(tokenCaptor.getValue().getExpiresAt());
        verify(emailService).sendConfirmation(
            "ada@example.com",
            "raw-secret-token"
        );
    }

    @Test
    void duplicateEmailIsRejectedBeforePasswordHashing() {
        when(userRepository.existsByEmailIgnoreCase("ada@example.com"))
            .thenReturn(true);

        assertThrows(
            ResponseStatusException.class,
            () -> service.register(new RegisterRequest(
                "Ada", "Lovelace", "ada@example.com",
                "706-555-0100", "Secure1!", false
            ))
        );
        verify(passwordService, never()).hash(any());
    }

    @Test
    void validVerificationTokenActivatesUserAndCannotRemainUnused() {
        User user = new User(
            "Ada", "Lovelace", "ada@example.com", "706-555-0100",
            "hash", User.Role.CUSTOMER, User.AccountStatus.INACTIVE, false
        );
        EmailVerificationToken token = new EmailVerificationToken(
            user, "token-hash", LocalDateTime.now().plusHours(1)
        );
        when(tokenService.hash("raw-token")).thenReturn("token-hash");
        when(tokenRepository.findByTokenHash("token-hash"))
            .thenReturn(Optional.of(token));

        service.verifyEmail("raw-token");

        assertEquals(User.AccountStatus.ACTIVE, user.getAccountStatus());
        assertNotNull(token.getUsedAt());
        verify(userRepository).save(user);
    }

    @Test
    void repeatedVerificationOfAnActivatedAccountIsSuccessful() {
        User user = new User(
            "Ada", "Lovelace", "ada@example.com", "706-555-0100",
            "hash", User.Role.CUSTOMER, User.AccountStatus.ACTIVE, false
        );
        EmailVerificationToken token = new EmailVerificationToken(
            user, "token-hash", LocalDateTime.now().plusHours(1)
        );
        token.markUsed();
        when(tokenService.hash("raw-token")).thenReturn("token-hash");
        when(tokenRepository.findByTokenHash("token-hash"))
            .thenReturn(Optional.of(token));

        service.verifyEmail("raw-token");

        assertEquals(User.AccountStatus.ACTIVE, user.getAccountStatus());
        verify(userRepository, never()).save(any(User.class));
    }
}
