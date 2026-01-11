package com.example.serverpublishingapp.controller;

import com.example.serverpublishingapp.entity.Material;
import com.example.serverpublishingapp.service.MaterialService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/materials")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping
    public List<Material> getAllMaterials() {
        return materialService.getAllMaterials();
    }

    @GetMapping("/category/{category}")
    public List<Material> getMaterialsByCategory(@PathVariable String category) {
        Material.Category cat;
        try {
            cat = Material.Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unknown category: " + category);
        }
        return materialService.getMaterialsByCategory(cat);
    }

    @PostMapping
    public Material createMaterial(@RequestBody Material material) {
        return materialService.createMaterial(material);
    }

    @PutMapping("/{id}")
    public Material updateMaterial(@PathVariable Long id, @RequestBody Material updated) {
        return materialService.updateMaterial(id, updated);
    }

    @DeleteMapping("/{id}")
    public void deleteMaterial(@PathVariable Long id) {
        materialService.deleteMaterial(id);
    }
}

