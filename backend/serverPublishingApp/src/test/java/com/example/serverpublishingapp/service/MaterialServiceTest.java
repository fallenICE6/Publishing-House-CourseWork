package com.example.serverpublishingapp.service;

import com.example.serverpublishingapp.entity.Material;
import com.example.serverpublishingapp.repository.MaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @Mock
    private MaterialRepository repository;

    @InjectMocks
    private MaterialService materialService;

    private Material material;

    @BeforeEach
    void setUp() {
        material = new Material();
        material.setId(1L);
        material.setName("Glossy Paper");
        material.setCategory(Material.Category.paper);
        material.setPrice(new BigDecimal("100.50"));
    }

    @Test
    void getAllMaterials_ShouldReturnAllMaterials() {
        when(repository.findAll()).thenReturn(Arrays.asList(material));

        List<Material> result = materialService.getAllMaterials();

        assertEquals(1, result.size());
        assertEquals("Glossy Paper", result.get(0).getName());
        verify(repository).findAll();
    }

    @Test
    void getMaterialsByCategory_ShouldReturnFilteredMaterials() {
        when(repository.findByCategory(Material.Category.paper)).thenReturn(Arrays.asList(material));

        List<Material> result = materialService.getMaterialsByCategory(Material.Category.paper);

        assertEquals(1, result.size());
        assertEquals(Material.Category.paper, result.get(0).getCategory());
        verify(repository).findByCategory(Material.Category.paper);
    }

    @Test
    void createMaterial_ShouldSaveMaterial() {
        when(repository.save(any(Material.class))).thenReturn(material);

        Material result = materialService.createMaterial(material);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository).save(material);
    }

    @Test
    void updateMaterial_ShouldUpdateExistingMaterial() {
        Material updated = new Material();
        updated.setName("Updated Paper");
        updated.setCategory(Material.Category.paper);
        updated.setPrice(new BigDecimal("150.00"));

        when(repository.findById(1L)).thenReturn(Optional.of(material));
        when(repository.save(any(Material.class))).thenReturn(material);

        Material result = materialService.updateMaterial(1L, updated);

        assertNotNull(result);
        assertEquals("Updated Paper", material.getName());
        verify(repository).save(material);
    }

    @Test
    void updateMaterial_ShouldThrowException_WhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> materialService.updateMaterial(99L, material));
        verify(repository, never()).save(any(Material.class));
    }

    @Test
    void deleteMaterial_ShouldDeleteMaterial() {
        materialService.deleteMaterial(1L);

        verify(repository).deleteById(1L);
    }
}