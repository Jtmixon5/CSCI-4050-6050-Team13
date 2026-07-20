package com.cinema.ebooking.service;

import com.cinema.ebooking.dto.AuthUserResponse;
import com.cinema.ebooking.dto.LoginRequest;
import com.cinema.ebooking.entity.User;
import com.cinema.ebooking.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {
    private UserRepository userRepository;
    private PasswordEncryptionService passwordService;
    private HttpSession session;
    private AuthService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordService = mock(PasswordEncryptionService.class);
        session = mock(HttpSession.class);
        service = new AuthService(userRepository, passwordService);
    }

    @Test
    void activeCustomerCanLoginAndCreatesSession() {
        User user = user(User.AccountStatus.ACTIVE);
        when(userRepository.findByEmailIgnoreCase("user@example.com"))
            .thenReturn(Optional.of(user));
        when(passwordService.matches("Secure1!", "hash")).thenReturn(true);

        AuthUserResponse response = service.login(
            new LoginRequest("USER@example.com", "Secure1!"),
            session
        );

        assertEquals("CUSTOMER", response.role());
        verify(session).setAttribute(AuthService.USER_ID, user.getId());
        verify(session).setAttribute(AuthService.USER_ROLE, "CUSTOMER");
    }

    @Test
    void inactiveCustomerCannotLogin() {
        User user = user(User.AccountStatus.INACTIVE);
        when(userRepository.findByEmailIgnoreCase("user@example.com"))
            .thenReturn(Optional.of(user));
        when(passwordService.matches("Secure1!", "hash")).thenReturn(true);

        assertThrows(
            ResponseStatusException.class,
            () -> service.login(
                new LoginRequest("user@example.com", "Secure1!"),
                session
            )
        );
    }

    private User user(User.AccountStatus status) {
        return new User(
            "Test", "Customer", "user@example.com", "706-555-0100",
            "hash", User.Role.CUSTOMER, status, false
        );
    }
}
