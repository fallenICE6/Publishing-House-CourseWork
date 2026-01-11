package com.example.serverpublishingapp.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/hash/{password}")
    public String generateHash(@PathVariable String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String hash = encoder.encode(password);
        System.out.println("Пароль: " + password);
        System.out.println("Хеш: " + hash);
        return hash;
    }
}