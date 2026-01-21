package com.example.serverpublishingapp.service;

import com.example.serverpublishingapp.entity.PublishingService;
import com.example.serverpublishingapp.repository.PublishingServiceRepository;
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
class PublishingServiceServiceTest {

    @Mock
    private PublishingServiceRepository repository;

    @InjectMocks
    private PublishingServiceService publishingServiceService;

    private PublishingService service;

    @BeforeEach
    void setUp() {
        service = new PublishingService();
        service.setId(1L);
        service.setTitle("Printing Service");
        service.setCategory("Printing");
        service.setPrice(new BigDecimal("5000.00"));
        service.setShortDescription("Short desc");
        service.setFullDescription("Full description");
        service.setImage("image.jpg");
    }

    @Test
    void getAll_ShouldReturnAllServices() {
        when(repository.findAll()).thenReturn(Arrays.asList(service));

        List<PublishingService> result = publishingServiceService.getAll();

        assertEquals(1, result.size());
        assertEquals("Printing Service", result.get(0).getTitle());
        verify(repository).findAll();
    }

    @Test
    void getByCategory_ShouldReturnFilteredServices() {
        when(repository.findByCategory("Printing")).thenReturn(Arrays.asList(service));

        List<PublishingService> result = publishingServiceService.getByCategory("Printing");

        assertEquals(1, result.size());
        assertEquals("Printing", result.get(0).getCategory());
        verify(repository).findByCategory("Printing");
    }

    @Test
    void getByCategories_ShouldReturnServicesFromMultipleCategories() {
        List<String> categories = Arrays.asList("Printing", "Design");
        when(repository.findByCategoryIn(categories)).thenReturn(Arrays.asList(service));

        List<PublishingService> result = publishingServiceService.getByCategories(categories);

        assertEquals(1, result.size());
        verify(repository).findByCategoryIn(categories);
    }

    @Test
    void getById_ShouldReturnService_WhenExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(service));

        PublishingService result = publishingServiceService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getById_ShouldThrowException_WhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> publishingServiceService.getById(99L));
    }

    @Test
    void create_ShouldSaveNewService() {
        when(repository.save(any(PublishingService.class))).thenReturn(service);

        PublishingService result = publishingServiceService.create(service);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository).save(service);
    }

    @Test
    void update_ShouldUpdateExistingService() {
        PublishingService updated = new PublishingService();
        updated.setTitle("Updated Service");
        updated.setCategory("Updated");
        updated.setPrice(new BigDecimal("6000.00"));

        when(repository.findById(1L)).thenReturn(Optional.of(service));
        when(repository.save(any(PublishingService.class))).thenReturn(service);

        PublishingService result = publishingServiceService.update(1L, updated);

        assertNotNull(result);
        assertEquals("Updated Service", service.getTitle());
        verify(repository).save(service);
    }

    @Test
    void delete_ShouldDeleteService() {
        publishingServiceService.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void getAllCategories_ShouldReturnUniqueCategories() {
        PublishingService service2 = new PublishingService();
        service2.setCategory("Design");

        when(repository.findAll()).thenReturn(Arrays.asList(service, service2));

        List<String> result = publishingServiceService.getAllCategories();

        assertEquals(2, result.size());
        assertTrue(result.contains("Printing"));
        assertTrue(result.contains("Design"));
    }
}