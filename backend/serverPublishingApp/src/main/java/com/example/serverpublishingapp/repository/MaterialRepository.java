package com.example.serverpublishingapp.repository;

import com.example.serverpublishingapp.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findByCategory(Material.Category category);
}
