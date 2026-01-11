package com.example.serverpublishingapp.service;

import com.example.serverpublishingapp.entity.Material;
import com.example.serverpublishingapp.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MaterialService {

    private final MaterialRepository repository;

    public MaterialService(MaterialRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Material> getAllMaterials() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Material> getMaterialsByCategory(Material.Category category) {
        return repository.findByCategory(category);
    }

    @Transactional
    public Material createMaterial(Material material) {
        return repository.save(material);
    }

    @Transactional
    public Material updateMaterial(Long id, Material updated) {
        Material material = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Material not found: " + id));
        material.setName(updated.getName());
        material.setCategory(updated.getCategory());
        material.setPrice(updated.getPrice());
        return repository.save(material);
    }

    @Transactional
    public void deleteMaterial(Long id) {
        repository.deleteById(id);
    }
}
