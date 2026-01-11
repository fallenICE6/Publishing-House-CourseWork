package com.example.serverpublishingapp.controller;

import com.example.serverpublishingapp.dto.LoginRequest;
import com.example.serverpublishingapp.dto.RegisterRequest;
import com.example.serverpublishingapp.dto.TokenResponse;
import com.example.serverpublishingapp.entity.User;
import com.example.serverpublishingapp.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@RequestBody RegisterRequest req) {

        User u = authService.register(req);
        String token = authService.loginAndGetToken(
                req.getUsername(),
                req.getPassword()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new TokenResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {
        String token = authService.loginAndGetToken(req.getUsername(), req.getPassword());
        return ResponseEntity.ok(new TokenResponse(token));
    }

}
