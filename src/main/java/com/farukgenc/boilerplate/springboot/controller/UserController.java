package com.farukgenc.boilerplate.springboot.controller;

import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.security.dto.UserUpdateRequest;
import com.farukgenc.boilerplate.springboot.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor // Using Lombok for constructor injection
public class UserController {

    private final UserService userService;

    @PutMapping("/me/update")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody UserUpdateRequest userUpdateRequest,
                                             @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            // This check might be redundant if Spring Security is configured to deny unauthenticated access.
            return ResponseEntity.status(401).body("User not authenticated");
        }

        try {
            User updatedUser = userService.updateUser(currentUser.getUsername(), userUpdateRequest);
            // Consider returning a DTO representation of the user instead of a generic message or the full entity.
            return ResponseEntity.ok("User profile updated successfully. New email: " + updatedUser.getEmail());
        } catch (RuntimeException e) {
            // Log the exception server-side for diagnostics.
            // Return a user-friendly error message.
            return ResponseEntity.badRequest().body("Error updating user profile: " + e.getMessage());
        }
    }
}
