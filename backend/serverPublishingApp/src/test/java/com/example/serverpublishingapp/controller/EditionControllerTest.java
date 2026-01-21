package com.example.serverpublishingapp.controller;

import com.example.serverpublishingapp.dto.EditionRequest;
import com.example.serverpublishingapp.dto.EditionResponse;
import com.example.serverpublishingapp.service.EditionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EditionControllerTest {

    @Mock
    private EditionService editionService;

    @InjectMocks
    private EditionController editionController;

    private EditionResponse editionResponse;

    @BeforeEach
    void setUp() {
        editionResponse = new EditionResponse();
        editionResponse.setId(1L);
        editionResponse.setTitle("Test Book");
        editionResponse.setAuthorFirstName("John");
        editionResponse.setAuthorLastName("Doe");
    }

    @Test
    void getAllEditions_ShouldReturnAllEditions() {
        when(editionService.getAllEditions()).thenReturn(Arrays.asList(editionResponse));

        ResponseEntity<List<EditionResponse>> response = editionController.getAllEditions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(editionService).getAllEditions();
    }

    @Test
    void getEditionById_ShouldReturnEdition() {
        when(editionService.getEditionById(1L)).thenReturn(editionResponse);

        ResponseEntity<EditionResponse> response = editionController.getEditionById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(editionService).getEditionById(1L);
    }

    @Test
    void getEditionsByGenre_ShouldReturnFilteredEditions() {
        when(editionService.getEditionsByGenre("Fiction")).thenReturn(Arrays.asList(editionResponse));

        ResponseEntity<List<EditionResponse>> response = editionController.getEditionsByGenre("Fiction");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(editionService).getEditionsByGenre("Fiction");
    }

    @Test
    void getAllGenres_ShouldReturnAllGenres() {
        when(editionService.getAllGenres()).thenReturn(Arrays.asList("Fiction", "Science"));

        ResponseEntity<List<String>> response = editionController.getAllGenres();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(editionService).getAllGenres();
    }

    @Test
    void searchEditions_ShouldReturnMatchingEditions() {
        when(editionService.searchEditions("Test")).thenReturn(Arrays.asList(editionResponse));

        ResponseEntity<List<EditionResponse>> response = editionController.searchEditions("Test");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(editionService).searchEditions("Test");
    }

    @Test
    void createEdition_ShouldCreateNewEdition() {
        EditionRequest request = new EditionRequest();
        request.setTitle("New Book");

        when(editionService.createEdition(any(EditionRequest.class))).thenReturn(editionResponse);

        ResponseEntity<EditionResponse> response = editionController.createEdition(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(editionService).createEdition(request);
    }

    @Test
    void updateEdition_ShouldUpdateExistingEdition() {
        EditionRequest request = new EditionRequest();
        request.setTitle("Updated Book");

        when(editionService.updateEdition(eq(1L), any(EditionRequest.class))).thenReturn(editionResponse);

        ResponseEntity<EditionResponse> response = editionController.updateEdition(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(editionService).updateEdition(1L, request);
    }

    @Test
    void deleteEdition_ShouldDeleteEdition() {
        doNothing().when(editionService).deleteEdition(1L);

        ResponseEntity<Void> response = editionController.deleteEdition(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(editionService).deleteEdition(1L);
    }
}