package com.cinema.ebooking.controller;

import com.cinema.ebooking.dto.UpdateProfileRequest;
import com.cinema.ebooking.dto.UserProfileResponse;
import com.cinema.ebooking.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(
        ProfileService profileService
    ) {
        this.profileService = profileService;
    }

    @GetMapping
    public UserProfileResponse getProfile(
        @PathVariable Long userId
    ) {
        return profileService.getProfile(userId);
    }

    @PutMapping
    public UserProfileResponse updateProfile(
        @PathVariable Long userId,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        return profileService.updateProfile(
            userId,
            request
        );
    }
}
