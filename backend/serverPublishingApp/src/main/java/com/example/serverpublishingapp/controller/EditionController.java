package com.example.serverpublishingapp.controller;

import com.example.serverpublishingapp.dto.EditionRequest;
import com.example.serverpublishingapp.dto.EditionResponse;
import com.example.serverpublishingapp.service.EditionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/editions")
public class EditionController {

    private final EditionService editionService;

    public EditionController(EditionService editionService) {
        this.editionService = editionService;
    }

    @GetMapping
    public ResponseEntity<List<EditionResponse>> getAllEditions() {
        return ResponseEntity.ok(editionService.getAllEditions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EditionResponse> getEditionById(@PathVariable Long id) {
        return ResponseEntity.ok(editionService.getEditionById(id));
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<EditionResponse>> getEditionsByGenre(@PathVariable String genre) {
        return ResponseEntity.ok(editionService.getEditionsByGenre(genre));
    }

    @GetMapping("/genres")
    public ResponseEntity<List<String>> getAllGenres() {
        return ResponseEntity.ok(editionService.getAllGenres());
    }

    @GetMapping("/search")
    public ResponseEntity<List<EditionResponse>> searchEditions(@RequestParam String query) {
        return ResponseEntity.ok(editionService.searchEditions(query));
    }

    @PostMapping
    public ResponseEntity<EditionResponse> createEdition(
            @Valid @RequestBody EditionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(editionService.createEdition(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EditionResponse> updateEdition(
            @PathVariable Long id,
            @Valid @RequestBody EditionRequest request) {
        return ResponseEntity.ok(editionService.updateEdition(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEdition(@PathVariable Long id) {
        editionService.deleteEdition(id);
        return ResponseEntity.noContent().build();
    }
}