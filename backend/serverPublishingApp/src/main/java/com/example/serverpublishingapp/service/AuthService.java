package com.example.serverpublishingapp.service;


import com.example.serverpublishingapp.dto.ChangePasswordRequest;
import com.example.serverpublishingapp.dto.RegisterRequest;
import com.example.serverpublishingapp.dto.UpdateUserRequest;
import com.example.serverpublishingapp.entity.Role;
import com.example.serverpublishingapp.entity.User;
import com.example.serverpublishingapp.jwt.JwtUtil;
import com.example.serverpublishingapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.BAD_REQUEST;


@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = users.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Текущий пароль неверен");
        }

        String hashed = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(hashed);

        users.save(user);
    }

    public User findByUsername(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public User register(RegisterRequest req) {

        if (users.existsByUsername(req.getUsername())) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "USERNAME_EXISTS"
            );
        }

        if (users.existsByPhone(req.getPhone())) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "PHONE_EXISTS"
            );
        }

        User u = new User();
        u.setUsername(req.getUsername());
        u.setPhone(req.getPhone());
        u.setEmail(req.getEmail());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setFirstName(req.getFirstName());
        u.setLastName(req.getLastName());
        u.setMiddleName(req.getMiddleName());
        u.setRole(Role.AUTHOR);

        return users.save(u);
    }

    public String loginAndGetToken(String username, String rawPassword) {
        User u = users.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!passwordEncoder.matches(rawPassword, u.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return jwtUtil.generateToken(u.getUsername(), u.getId(), u.getRole().name());
    }

    @Transactional
    public User updateProfile(String username, UpdateUserRequest request) {
        User user = findByUsername(username);

        if (request.phone() != null && !request.phone().equals(user.getPhone()) &&
                users.existsByPhone(request.phone())) {
            throw new RuntimeException("PHONE_EXISTS");
        }

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setMiddleName(request.middleName());
        user.setEmail(request.email());
        user.setPhone(request.phone());

        return users.save(user);
    }
}
