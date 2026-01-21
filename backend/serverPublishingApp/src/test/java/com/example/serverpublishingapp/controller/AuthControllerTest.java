package com.example.serverpublishingapp.controller;

import com.example.serverpublishingapp.dto.LoginRequest;
import com.example.serverpublishingapp.dto.RegisterRequest;
import com.example.serverpublishingapp.dto.TokenResponse;
import com.example.serverpublishingapp.entity.User;
import com.example.serverpublishingapp.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password");
        registerRequest.setEmail("test@example.com");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
    }

    @Test
    void register_ShouldReturnToken() {
        when(authService.register(any(RegisterRequest.class))).thenReturn(user);
        when(authService.loginAndGetToken("testuser", "password")).thenReturn("jwt-token");

        ResponseEntity<TokenResponse> response = authController.register(registerRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt-token", response.getBody().getToken());
        verify(authService).register(registerRequest);
        verify(authService).loginAndGetToken("testuser", "password");
    }

    @Test
    void login_ShouldReturnToken() {
        when(authService.loginAndGetToken("testuser", "password")).thenReturn("jwt-token");

        ResponseEntity<TokenResponse> response = authController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt-token", response.getBody().getToken());
        verify(authService).loginAndGetToken("testuser", "password");
    }
}