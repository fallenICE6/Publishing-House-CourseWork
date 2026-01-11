package com.example.serverpublishingapp;

import com.example.serverpublishingapp.jwt.JwtUtil;

public class JwtTestGenerator {
    public static void main(String[] args) {
        // Ручной секрет, независимый от Spring
        String secret = "mysupersecretkey_forjwtmustbe32charsmin";
        long validity = 3600000; // 1 час

        // Используем чистый конструктор JwtUtil
        JwtUtil jwtUtil = new JwtUtil(secret, validity);

        String username = "matvey123";
        Long userId = 2L;
        String role = "AUTHOR";

        String token = jwtUtil.generateToken(username, userId, role);
        System.out.println("NEW JWT FOR POSTMAN: " + token);
    }
}
