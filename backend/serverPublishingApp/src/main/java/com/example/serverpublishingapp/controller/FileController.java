package com.example.serverpublishingapp.controller;

import com.example.serverpublishingapp.entity.OrderFile;
import com.example.serverpublishingapp.entity.User;
import com.example.serverpublishingapp.repository.OrderFileRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final OrderFileRepository orderFileRepository;

    public FileController(OrderFileRepository orderFileRepository) {
        this.orderFileRepository = orderFileRepository;
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        OrderFile orderFile = orderFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));


        try {
            Path filePath = Paths.get(orderFile.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("File not found or not readable");
            }

            String contentType = orderFile.getFileType() != null ?
                    orderFile.getFileType() : "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + orderFile.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            throw new RuntimeException("Could not download file: " + orderFile.getFileName());
        }
    }
}