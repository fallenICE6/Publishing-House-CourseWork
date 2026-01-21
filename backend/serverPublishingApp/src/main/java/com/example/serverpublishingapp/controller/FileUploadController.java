package com.example.serverpublishingapp.controller;

import com.example.serverpublishingapp.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileStorageService storage;

    public FileUploadController(FileStorageService storage) {
        this.storage = storage;
    }

    @PostMapping("/upload")
    public ResponseEntity<List<String>> upload(
            @RequestPart(required = false) MultipartFile cover,
            @RequestPart(required = false) List<MultipartFile> images
    ) {
        List<String> urls = new ArrayList<>();

        if (cover != null) {
            urls.add(storage.save(cover, "covers"));
        }
        if (images != null) {
            for (MultipartFile img : images) {
                urls.add(storage.save(img, "interior"));
            }
        }
        return ResponseEntity.ok(urls);
    }
}

