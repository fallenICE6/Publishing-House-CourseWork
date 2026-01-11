package com.example.serverpublishingapp.dto;

import java.time.LocalDateTime;
import java.util.List;

public class EditionResponse {
    private Long id;
    private String title;
    private String authorFirstName;
    private String authorLastName;
    private String authorMiddleName;
    private String fullAuthorName;
    private String description;
    private String coverImage;
    private List<String> genres;
    private List<String> interiorImages;


    public EditionResponse() {}

    public EditionResponse(Long id, String title,
                           String authorFirstName, String authorLastName, String authorMiddleName,
                           String description, String coverImage,
                           List<String> genres, List<String> interiorImages) {
        this.id = id;
        this.title = title;
        this.authorFirstName = authorFirstName;
        this.authorLastName = authorLastName;
        this.authorMiddleName = authorMiddleName;
        this.fullAuthorName = buildFullName(authorFirstName, authorMiddleName, authorLastName);
        this.description = description;
        this.coverImage = coverImage;
        this.genres = genres;
        this.interiorImages = interiorImages;
    }

    private String buildFullName(String first, String middle, String last) {
        StringBuilder sb = new StringBuilder();
        if (first != null && !first.trim().isEmpty()) sb.append(first).append(" ");
        if (middle != null && !middle.trim().isEmpty()) sb.append(middle).append(" ");
        if (last != null && !last.trim().isEmpty()) sb.append(last);
        return sb.toString().trim();
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthorFirstName() { return authorFirstName; }
    public void setAuthorFirstName(String authorFirstName) { this.authorFirstName = authorFirstName; }

    public String getAuthorLastName() { return authorLastName; }
    public void setAuthorLastName(String authorLastName) { this.authorLastName = authorLastName; }

    public String getAuthorMiddleName() { return authorMiddleName; }
    public void setAuthorMiddleName(String authorMiddleName) { this.authorMiddleName = authorMiddleName; }

    public String getFullAuthorName() { return fullAuthorName; }
    public void setFullAuthorName(String fullAuthorName) { this.fullAuthorName = fullAuthorName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public List<String> getInteriorImages() { return interiorImages; }
    public void setInteriorImages(List<String> interiorImages) { this.interiorImages = interiorImages; }

}