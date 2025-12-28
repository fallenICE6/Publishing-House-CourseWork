package com.example.serverpublishingapp.service;


import com.example.serverpublishingapp.entity.Role;
import com.example.serverpublishingapp.entity.User;
import com.example.serverpublishingapp.jwt.JwtUtil;
import com.example.serverpublishingapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
    public User register(String username, String email, String rawPassword,
                         String firstName, String lastName, String middleName) {
        if (users.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (users.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setMiddleName(middleName);
        u.setRole(Role.USER);
        return users.save(u);
    }

    public String loginAndGetToken(String username, String rawPassword) {
        User u = users.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!passwordEncoder.matches(rawPassword, u.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return jwtUtil.generateToken(u.getUsername(), u.getId(), u.getRole().getDbValue());
    }
}
