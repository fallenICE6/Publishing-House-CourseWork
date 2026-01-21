package com.example.serverpublishingapp.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Created upload directory: " + uploadPath.toAbsolutePath());
            }

            Path coversPath = uploadPath.resolve("covers");
            Path interiorPath = uploadPath.resolve("interior");

            if (!Files.exists(coversPath)) {
                Files.createDirectories(coversPath);
                System.out.println("Created covers directory: " + coversPath.toAbsolutePath());
            }

            if (!Files.exists(interiorPath)) {
                Files.createDirectories(interiorPath);
                System.out.println("Created interior directory: " + interiorPath.toAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Could not create upload directories: " + e.getMessage());
            throw new RuntimeException("Could not create upload directories", e);
        }
    }

    public String save(MultipartFile file, String folder) {
        try {
            String filename = UUID.randomUUID() + "_" +
                    (file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");

            Path path = Paths.get(uploadDir, folder, filename);
            Files.createDirectories(path.getParent());
            Files.copy(file.getInputStream(), path);

            return "/uploads/" + folder + "/" + filename;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки файла: " + e.getMessage());
        }
    }
}