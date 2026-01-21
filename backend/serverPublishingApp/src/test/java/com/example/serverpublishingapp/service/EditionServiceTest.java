package com.example.serverpublishingapp.service;

import com.example.serverpublishingapp.dto.EditionRequest;
import com.example.serverpublishingapp.dto.EditionResponse;
import com.example.serverpublishingapp.entity.Edition;
import com.example.serverpublishingapp.entity.EditionImage;
import com.example.serverpublishingapp.entity.Genre;
import com.example.serverpublishingapp.repository.EditionRepository;
import com.example.serverpublishingapp.repository.GenreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EditionServiceTest {

    @Mock
    private EditionRepository editionRepository;

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private EditionService editionService;

    private Edition edition;
    private EditionRequest editionRequest;
    private Genre genre;

    @BeforeEach
    void setUp() {
        genre = new Genre();
        genre.setId(1L);
        genre.setName("Fiction");

        edition = new Edition();
        edition.setId(1L);
        edition.setTitle("Test Book");
        edition.setAuthorFirstName("John");
        edition.setAuthorLastName("Doe");
        edition.setAuthorMiddleName("Middle");
        edition.setDescription("Test description");
        edition.setCoverImage("cover.jpg");

        // Инициализируем списки перед использованием
        edition.setGenres(new ArrayList<>(Arrays.asList(genre)));
        edition.setInteriorImages(new ArrayList<>());

        EditionImage image = new EditionImage();
        image.setImageName("interior.jpg");
        edition.getInteriorImages().add(image);

        editionRequest = new EditionRequest();
        editionRequest.setTitle("New Book");
        editionRequest.setAuthorFirstName("Jane");
        editionRequest.setAuthorLastName("Smith");
        editionRequest.setAuthorMiddleName("Middle");
        editionRequest.setDescription("New description");
        editionRequest.setCoverImage("new_cover.jpg");
        editionRequest.setGenres(Arrays.asList("Fiction"));
        editionRequest.setInteriorImages(Arrays.asList("img1.jpg", "img2.jpg"));
    }

    @Test
    void getAllEditions_ShouldReturnAllEditions() {
        // Arrange
        when(editionRepository.findAll()).thenReturn(Arrays.asList(edition));

        // Act
        List<EditionResponse> result = editionService.getAllEditions();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getTitle());
        verify(editionRepository).findAll();
    }

    @Test
    void getEditionById_ShouldReturnEdition_WhenExists() {
        // Arrange
        when(editionRepository.findById(1L)).thenReturn(Optional.of(edition));

        // Act
        EditionResponse result = editionService.getEditionById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Book", result.getTitle());
        verify(editionRepository).findById(1L);
    }

    @Test
    void getEditionById_ShouldThrowException_WhenNotFound() {
        // Arrange
        when(editionRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> editionService.getEditionById(99L));
    }

    @Test
    void getEditionsByGenre_ShouldReturnFilteredEditions() {
        // Arrange
        when(editionRepository.findByGenreName("Fiction")).thenReturn(Arrays.asList(edition));

        // Act
        List<EditionResponse> result = editionService.getEditionsByGenre("Fiction");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getTitle());
        verify(editionRepository).findByGenreName("Fiction");
    }

    @Test
    void getAllGenres_ShouldReturnGenreNames() {
        // Arrange
        when(editionRepository.findAllGenreNames()).thenReturn(Arrays.asList("Fiction", "Science"));

        // Act
        List<String> result = editionService.getAllGenres();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains("Fiction"));
        assertTrue(result.contains("Science"));
    }

    @Test
    void searchEditions_ShouldReturnMatchingEditions() {
        // Arrange
        when(editionRepository.findByTitle("Test")).thenReturn(Arrays.asList(edition));

        // Act
        List<EditionResponse> result = editionService.searchEditions("Test");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getTitle());
        verify(editionRepository).findByTitle("Test");
    }

    @Test
    void createEdition_ShouldSaveNewEdition() {
        // Arrange
        when(genreRepository.findByNameIn(anyList())).thenReturn(Arrays.asList(genre));

        Edition savedEdition = new Edition();
        savedEdition.setId(1L);
        savedEdition.setTitle("New Book");
        savedEdition.setAuthorFirstName("Jane");
        savedEdition.setAuthorLastName("Smith");
        savedEdition.setAuthorMiddleName("Middle");
        savedEdition.setDescription("New description");
        savedEdition.setCoverImage("new_cover.jpg");
        savedEdition.setGenres(new ArrayList<>(Arrays.asList(genre)));
        savedEdition.setInteriorImages(new ArrayList<>());

        when(editionRepository.save(any(Edition.class))).thenReturn(savedEdition);

        EditionResponse result = editionService.createEdition(editionRequest);

        assertNotNull(result);
        assertEquals("New Book", result.getTitle());
        verify(editionRepository).save(any(Edition.class));
        verify(genreRepository).findByNameIn(Arrays.asList("Fiction"));
    }

    @Test
    void updateEdition_ShouldUpdateExistingEdition() {
        // Arrange - создаем edition с инициализированными списками
        Edition existingEdition = new Edition();
        existingEdition.setId(1L);
        existingEdition.setTitle("Old Book");
        existingEdition.setGenres(new ArrayList<>(Arrays.asList(genre)));
        existingEdition.setInteriorImages(new ArrayList<>());

        when(editionRepository.findById(1L)).thenReturn(Optional.of(existingEdition));
        when(genreRepository.findByNameIn(anyList())).thenReturn(Arrays.asList(genre));
        when(editionRepository.save(any(Edition.class))).thenReturn(existingEdition);

        // Act
        EditionResponse result = editionService.updateEdition(1L, editionRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(editionRepository).save(any(Edition.class));
    }

    @Test
    void updateEdition_ShouldThrowException_WhenNotFound() {
        // Arrange
        when(editionRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> editionService.updateEdition(99L, editionRequest));
        verify(editionRepository, never()).save(any(Edition.class));
    }

    @Test
    void deleteEdition_ShouldDelete_WhenExists() {
        // Arrange
        when(editionRepository.existsById(1L)).thenReturn(true);

        // Act
        editionService.deleteEdition(1L);

        // Assert
        verify(editionRepository).deleteById(1L);
    }

    @Test
    void deleteEdition_ShouldThrowException_WhenNotFound() {
        // Arrange
        when(editionRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> editionService.deleteEdition(99L));
        verify(editionRepository, never()).deleteById(anyLong());
    }
}