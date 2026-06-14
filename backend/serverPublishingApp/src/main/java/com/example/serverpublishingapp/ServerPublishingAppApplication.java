package com.example.serverpublishingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ServerPublishingAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerPublishingAppApplication.class, args);
    }

}
