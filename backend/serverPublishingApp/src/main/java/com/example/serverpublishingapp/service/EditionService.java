package com.example.serverpublishingapp.service;

import com.example.serverpublishingapp.dto.EditionRequest;
import com.example.serverpublishingapp.dto.EditionResponse;
import com.example.serverpublishingapp.entity.Edition;
import com.example.serverpublishingapp.entity.EditionImage;
import com.example.serverpublishingapp.entity.Genre;
import com.example.serverpublishingapp.repository.EditionRepository;
import com.example.serverpublishingapp.repository.GenreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EditionService {

    private final EditionRepository editionRepository;
    private final GenreRepository genreRepository;

    public EditionService(EditionRepository editionRepository, GenreRepository genreRepository) {
        this.editionRepository = editionRepository;
        this.genreRepository = genreRepository;
    }

    @Transactional(readOnly = true)
    public List<EditionResponse> getAllEditions() {
        return editionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EditionResponse getEditionById(Long id) {
        Edition edition = editionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Издание не найдено"));
        return mapToResponse(edition);
    }

    @Transactional(readOnly = true)
    public List<EditionResponse> getEditionsByGenre(String genre) {
        return editionRepository.findByGenreName(genre).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getAllGenres() {
        return editionRepository.findAllGenreNames();
    }

    @Transactional(readOnly = true)
    public List<EditionResponse> searchEditions(String query) {
        List<Edition> byTitle = editionRepository.findByTitle(query);
        List<Edition> byAuthor = editionRepository.findByAuthorName(query);

        return byTitle.stream()
                .collect(Collectors.toList())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EditionResponse createEdition(EditionRequest request) {
        Edition edition = new Edition();
        updateEditionFromRequest(edition, request);
        editionRepository.save(edition);
        return mapToResponse(edition);
    }

    @Transactional
    public EditionResponse updateEdition(Long id, EditionRequest request) {
        Edition edition = editionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Издание не найдено"));
        updateEditionFromRequest(edition, request);
        editionRepository.save(edition);
        return mapToResponse(edition);
    }

    // Удалить издание
    @Transactional
    public void deleteEdition(Long id) {
        if (!editionRepository.existsById(id)) {
            throw new IllegalArgumentException("Издание не найдено");
        }
        editionRepository.deleteById(id);
    }

    private void updateEditionFromRequest(Edition edition, EditionRequest request) {
        edition.setTitle(request.getTitle());
        edition.setAuthorFirstName(request.getAuthorFirstName());
        edition.setAuthorLastName(request.getAuthorLastName());
        edition.setAuthorMiddleName(request.getAuthorMiddleName());
        edition.setDescription(request.getDescription());
        edition.setCoverImage(request.getCoverImage());

        if (request.getGenres() != null) {
            List<Genre> genres = genreRepository.findByNameIn(request.getGenres());
            edition.setGenres(genres);
        }

        if (request.getInteriorImages() != null) {
            edition.getInteriorImages().clear();
            for (int i = 0; i < request.getInteriorImages().size(); i++) {
                EditionImage image = new EditionImage();
                image.setEdition(edition);
                image.setImageName(request.getInteriorImages().get(i));
                image.setDisplayOrder(i);
                edition.getInteriorImages().add(image);
            }
        }
    }

    private EditionResponse mapToResponse(Edition edition) {
        List<String> genreNames = edition.getGenres().stream()
                .map(Genre::getName)
                .collect(Collectors.toList());

        List<String> imageNames = edition.getInteriorImages().stream()
                .map(EditionImage::getImageName)
                .collect(Collectors.toList());

        return new EditionResponse(
                edition.getId(),
                edition.getTitle(),
                edition.getAuthorFirstName(),
                edition.getAuthorLastName(),
                edition.getAuthorMiddleName(),
                edition.getDescription(),
                edition.getCoverImage(),
                genreNames,
                imageNames
        );
    }
}