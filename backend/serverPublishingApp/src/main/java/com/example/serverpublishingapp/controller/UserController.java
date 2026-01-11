package com.example.serverpublishingapp.controller;

import com.example.serverpublishingapp.dto.ChangePasswordRequest;
import com.example.serverpublishingapp.dto.UpdateUserRequest;
import com.example.serverpublishingapp.dto.UserResponse;
import com.example.serverpublishingapp.entity.User;
import com.example.serverpublishingapp.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }


    @GetMapping("/{username}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable String username,
            Authentication auth
    ) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String currentUsername = auth.getName();
        if (!username.equals(currentUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User user = authService.findByUsername(username);

        return ResponseEntity.ok(toResponse(user));
    }


    @PutMapping("/{username}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String username,
            @RequestBody UpdateUserRequest request,
            Authentication auth
    ) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String currentUsername = auth.getName();
        if (!username.equals(currentUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User updatedUser = authService.updateProfile(username, request);

        return ResponseEntity.ok(toResponse(updatedUser));
    }

    @PutMapping("/{username}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable String username,
            @RequestBody ChangePasswordRequest request,
            Authentication auth) {

        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!username.equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        authService.changePassword(username, request);

        return ResponseEntity.ok().build();
    }


    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getPhone(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getMiddleName(),
                user.getRole().name()
        );
    }
}
