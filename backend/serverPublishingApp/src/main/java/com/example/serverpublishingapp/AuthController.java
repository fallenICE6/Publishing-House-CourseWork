package com.example.serverpublishingapp;


import com.example.serverpublishingapp.dto.LoginRequest;
import com.example.serverpublishingapp.dto.RegisterRequest;
import com.example.serverpublishingapp.dto.TokenResponse;
import com.example.serverpublishingapp.entity.User;
import com.example.serverpublishingapp.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@RequestBody RegisterRequest req) {
        User u = authService.register(req.getUsername(), req.getEmail(), req.getPassword(),
                req.getFirstName(), req.getLastName(), req.getMiddleName());
        String token = authService.loginAndGetToken(u.getUsername(), req.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(new TokenResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {
        String token = authService.loginAndGetToken(req.getUsername(), req.getPassword());
        return ResponseEntity.ok(new TokenResponse(token));
    }
}
