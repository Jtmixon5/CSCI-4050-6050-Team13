package com.cinema.ebooking.controller;

import com.cinema.ebooking.dto.UpdateProfileRequest;
import com.cinema.ebooking.dto.UserProfileResponse;
import com.cinema.ebooking.service.ProfileService;
import com.cinema.ebooking.service.AuthService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final AuthService authService;

    public ProfileController(
        ProfileService profileService,
        AuthService authService
    ) {
        this.profileService = profileService;
        this.authService = authService;
    }

    @GetMapping
    public UserProfileResponse getProfile(
        HttpSession session
    ) {
        return profileService.getProfile(
            authService.requireCurrentUser(session).getId()
        );
    }

    @PutMapping
    public UserProfileResponse updateProfile(
        @Valid @RequestBody UpdateProfileRequest request,
        HttpSession session
    ) {
        return profileService.updateProfile(
            authService.requireCurrentUser(session).getId(),
            request
        );
    }
}
