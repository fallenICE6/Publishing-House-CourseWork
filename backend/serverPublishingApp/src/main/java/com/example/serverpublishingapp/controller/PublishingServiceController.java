package com.example.serverpublishingapp.controller;

import com.example.serverpublishingapp.entity.PublishingService;
import com.example.serverpublishingapp.service.PublishingServiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class PublishingServiceController {

    private final PublishingServiceService service;

    public PublishingServiceController(PublishingServiceService service) {
        this.service = service;
    }

    @GetMapping
    public List<PublishingService> getAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> categories
    ) {
        if (category != null) {
            return service.getByCategory(category);
        }
        if (categories != null && !categories.isEmpty()) {
            return service.getByCategories(categories);
        }
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublishingService> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        return ResponseEntity.ok(service.getAllCategories());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PublishingService> create(@RequestBody PublishingService publishingService) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(publishingService));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PublishingService> update(
            @PathVariable Long id,
            @RequestBody PublishingService publishingService) {
        return ResponseEntity.ok(service.update(id, publishingService));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}