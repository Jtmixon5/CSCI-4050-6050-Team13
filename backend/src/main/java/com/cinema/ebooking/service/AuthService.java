package com.cinema.ebooking.service;

import com.cinema.ebooking.dto.AuthUserResponse;
import com.cinema.ebooking.dto.ChangePasswordRequest;
import com.cinema.ebooking.dto.LoginRequest;
import com.cinema.ebooking.entity.User;
import com.cinema.ebooking.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    public static final String USER_ID = "authenticatedUserId";
    public static final String USER_ROLE = "authenticatedUserRole";

    private final UserRepository userRepository;
    private final PasswordEncryptionService passwordService;
    private final ConcurrentHashMap<String, LoginAttempts> loginAttempts =
        new ConcurrentHashMap<>();

    public AuthService(
        UserRepository userRepository,
        PasswordEncryptionService passwordService
    ) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    @Transactional(readOnly = true)
    public AuthUserResponse login(LoginRequest request, HttpSession session) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        ensureNotRateLimited(email);
        User user = userRepository
            .findByEmailIgnoreCase(email)
            .orElseThrow(() -> {
                recordFailure(email);
                return invalidCredentials();
            });

        if (!passwordService.matches(request.password(), user.getPasswordHash())) {
            recordFailure(email);
            throw invalidCredentials();
        }

        if (user.getAccountStatus() == User.AccountStatus.INACTIVE) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Confirm your email address before signing in."
            );
        }
        if (user.getAccountStatus() == User.AccountStatus.SUSPENDED) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "This account is suspended."
            );
        }

        session.setAttribute(USER_ID, user.getId());
        session.setAttribute(USER_ROLE, user.getRole().name());
        loginAttempts.remove(email);
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthUserResponse currentUser(HttpSession session) {
        return toResponse(requireCurrentUser(session));
    }

    @Transactional
    public void changePassword(
        ChangePasswordRequest request,
        HttpSession session
    ) {
        User user = requireCurrentUser(session);
        if (!passwordService.matches(
            request.currentPassword(),
            user.getPasswordHash()
        )) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Current password is incorrect."
            );
        }
        if (passwordService.matches(
            request.newPassword(),
            user.getPasswordHash()
        )) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "New password must be different from the current password."
            );
        }
        user.changePassword(passwordService.hash(request.newPassword()));
        userRepository.save(user);
    }

    public User requireCurrentUser(HttpSession session) {
        Object value = session.getAttribute(USER_ID);
        if (!(value instanceof Long userId)) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Sign in to continue."
            );
        }
        User user = userRepository.findById(userId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session is invalid.")
        );
        if (user.getAccountStatus() != User.AccountStatus.ACTIVE) {
            session.invalidate();
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "This session is no longer active."
            );
        }
        return user;
    }

    private AuthUserResponse toResponse(User user) {
        return new AuthUserResponse(
            user.getId(),
            user.getFirstName(),
            user.getEmail(),
            user.getRole().name()
        );
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Email or password is incorrect."
        );
    }

    private void ensureNotRateLimited(String email) {
        LoginAttempts attempts = loginAttempts.get(email);
        if (attempts != null
            && attempts.failures >= 5
            && attempts.firstFailure.isAfter(
                Instant.now().minus(15, ChronoUnit.MINUTES)
            )) {
            throw new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too many failed attempts. Try again in 15 minutes."
            );
        }
        if (attempts != null
            && attempts.firstFailure.isBefore(
                Instant.now().minus(15, ChronoUnit.MINUTES)
            )) {
            loginAttempts.remove(email);
        }
    }

    private void recordFailure(String email) {
        loginAttempts.compute(
            email,
            (key, attempts) ->
                attempts == null
                    ? new LoginAttempts(1, Instant.now())
                    : new LoginAttempts(
                        attempts.failures + 1,
                        attempts.firstFailure
                    )
        );
    }

    private record LoginAttempts(int failures, Instant firstFailure) {
    }
}
