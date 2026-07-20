package com.cinema.ebooking.controller;

import com.cinema.ebooking.dto.MessageResponse;
import com.cinema.ebooking.dto.RegisterRequest;
import com.cinema.ebooking.dto.AuthUserResponse;
import com.cinema.ebooking.dto.ChangePasswordRequest;
import com.cinema.ebooking.dto.EmailRequest;
import com.cinema.ebooking.dto.LoginRequest;
import com.cinema.ebooking.dto.ResetPasswordRequest;
import com.cinema.ebooking.service.AuthService;
import com.cinema.ebooking.service.PasswordResetService;
import com.cinema.ebooking.service.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.web.csrf.CsrfToken;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(
        RegistrationService registrationService,
        AuthService authService,
        PasswordResetService passwordResetService
    ) {
        this.registrationService = registrationService;
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse register(
        @Valid @RequestBody RegisterRequest request
    ) {
        registrationService.register(request);
        return new MessageResponse(
            "Registration successful. Check your email to activate your account."
        );
    }

    @GetMapping("/verify")
    public MessageResponse verifyEmail(@RequestParam String token) {
        registrationService.verifyEmail(token);
        return new MessageResponse(
            "Email verified successfully. Your account is now active."
        );
    }

    @PostMapping("/login")
    public AuthUserResponse login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest servletRequest
    ) {
        HttpSession session = servletRequest.getSession(true);
        servletRequest.changeSessionId();
        return authService.login(request, session);
    }

    @GetMapping("/me")
    public AuthUserResponse currentUser(HttpSession session) {
        return authService.currentUser(session);
    }

    @PostMapping("/logout")
    public MessageResponse logout(HttpSession session) {
        session.invalidate();
        return new MessageResponse("You have been signed out.");
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(
        @Valid @RequestBody EmailRequest request
    ) {
        passwordResetService.requestReset(request.email());
        return new MessageResponse(
            "If an account exists for that email, a reset link has been sent."
        );
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(
        @Valid @RequestBody ResetPasswordRequest request
    ) {
        passwordResetService.reset(request);
        return new MessageResponse(
            "Password reset successfully. You can now sign in."
        );
    }

    @PostMapping("/change-password")
    public MessageResponse changePassword(
        @Valid @RequestBody ChangePasswordRequest request,
        HttpSession session
    ) {
        authService.changePassword(request, session);
        return new MessageResponse("Password changed successfully.");
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of(
            "headerName", token.getHeaderName(),
            "token", token.getToken()
        );
    }
}
