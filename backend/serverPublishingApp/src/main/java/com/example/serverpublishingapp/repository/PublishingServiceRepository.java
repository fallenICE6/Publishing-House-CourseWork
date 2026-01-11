package com.example.serverpublishingapp.repository;

import com.example.serverpublishingapp.entity.PublishingService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublishingServiceRepository extends JpaRepository<PublishingService, Long> {
    List<PublishingService> findByCategory(String category);
    List<PublishingService> findByCategoryIn(List<String> categories);
}