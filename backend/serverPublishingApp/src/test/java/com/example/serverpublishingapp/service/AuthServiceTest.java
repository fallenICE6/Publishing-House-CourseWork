package com.example.serverpublishingapp.service;

import com.example.serverpublishingapp.dto.ChangePasswordRequest;
import com.example.serverpublishingapp.dto.RegisterRequest;
import com.example.serverpublishingapp.dto.UpdateUserRequest;
import com.example.serverpublishingapp.entity.Role;
import com.example.serverpublishingapp.entity.User;
import com.example.serverpublishingapp.jwt.JwtUtil;
import com.example.serverpublishingapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setPhone("+123456789");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setMiddleName("Middle");
        testUser.setRole(Role.AUTHOR);

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPhone("+123456789");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setMiddleName("Middle");
    }

    @Test
    void register_ShouldSaveUser_WhenUsernameAndPhoneAreUnique() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(Role.AUTHOR, result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenUsernameExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenPhoneExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginAndGetToken_ShouldReturnToken_WhenCredentialsAreValid() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString())).thenReturn("jwt-token");

        String token = authService.loginAndGetToken("testuser", "password");

        assertNotNull(token);
        assertEquals("jwt-token", token);
    }

    @Test
    void loginAndGetToken_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> authService.loginAndGetToken("unknown", "password"));
    }

    @Test
    void loginAndGetToken_ShouldThrowException_WhenPasswordInvalid() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> authService.loginAndGetToken("testuser", "wrong"));
    }

    @Test
    void changePassword_ShouldUpdatePassword_WhenCurrentPasswordIsCorrect() {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPass", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("newEncodedPassword");

        authService.changePassword("testuser", request);

        verify(passwordEncoder).encode("newPass");
        verify(userRepository).save(testUser);
    }

    @Test
    void changePassword_ShouldThrowException_WhenCurrentPasswordIsWrong() {
        ChangePasswordRequest request = new ChangePasswordRequest("wrong", "newPass");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> authService.changePassword("testuser", request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateProfile_ShouldUpdateUser_WhenPhoneIsUnique() {
        UpdateUserRequest request = new UpdateUserRequest(
                "Updated", "User", "Middle",
                "updated@example.com", "+987654321"
        );
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByPhone("+987654321")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.updateProfile("testuser", request);

        assertNotNull(result);
        verify(userRepository).save(testUser);
    }

    @Test
    void updateProfile_ShouldThrowException_WhenPhoneAlreadyExists() {
        UpdateUserRequest request = new UpdateUserRequest(
                null, null, null,
                null, "+987654321"
        );
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByPhone("+987654321")).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> authService.updateProfile("testuser", request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findAllUsers_ShouldReturnAllUsers_WhenNoSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<User> result = authService.findAllUsers(null, pageable);

        assertEquals(1, result.getContent().size());
        verify(userRepository).findAll(pageable);
    }

    @Test
    void findAllUsers_ShouldSearch_WhenSearchTermProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findByUsernameContainingIgnoreCase("test", pageable))
                .thenReturn(userPage);

        Page<User> result = authService.findAllUsers("test", pageable);

        assertEquals(1, result.getContent().size());
        verify(userRepository).findByUsernameContainingIgnoreCase("test", pageable);
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = authService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void findById_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.findById(99L));
    }

    @Test
    void changeUserRole_ShouldUpdateRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.changeUserRole(1L, "REVIEWER");

        assertEquals(Role.REVIEWER, result.getRole());
        verify(userRepository).save(testUser);
    }

    @Test
    void changeUserRole_ShouldThrowException_WhenInvalidRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(RuntimeException.class,
                () -> authService.changeUserRole(1L, "INVALID_ROLE"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void searchByUsername_ShouldReturnMatchingUsers() {
        when(userRepository.findByUsernameContainingIgnoreCase("test"))
                .thenReturn(Arrays.asList(testUser));

        List<User> result = authService.searchByUsername("test");

        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }
}