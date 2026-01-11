package com.example.serverpublishingapp.service;

import com.example.serverpublishingapp.entity.PublishingService;
import com.example.serverpublishingapp.repository.PublishingServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PublishingServiceService {

    private final PublishingServiceRepository repository;

    public PublishingServiceService(PublishingServiceRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PublishingService> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PublishingService> getByCategory(String category) {
        return repository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public List<PublishingService> getByCategories(List<String> categories) {
        return repository.findByCategoryIn(categories);
    }

    @Transactional(readOnly = true)
    public PublishingService getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found with id: " + id));
    }

    @Transactional
    public PublishingService create(PublishingService service) {
        return repository.save(service);
    }

    @Transactional
    public PublishingService update(Long id, PublishingService serviceDetails) {
        PublishingService service = getById(id);

        service.setTitle(serviceDetails.getTitle());
        service.setShortDescription(serviceDetails.getShortDescription());
        service.setFullDescription(serviceDetails.getFullDescription());
        service.setPrice(serviceDetails.getPrice());
        service.setImage(serviceDetails.getImage());
        service.setCategory(serviceDetails.getCategory());

        return repository.save(service);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return repository.findAll().stream()
                .map(PublishingService::getCategory)
                .distinct()
                .toList();
    }
}