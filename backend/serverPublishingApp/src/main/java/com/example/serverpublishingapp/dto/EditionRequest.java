package com.example.serverpublishingapp.dto;

import java.util.List;

public class EditionRequest {
    private String title;
    private String authorFirstName;
    private String authorLastName;
    private String authorMiddleName;
    private String description;
    private String coverImage;
    private List<String> genres;
    private List<String> interiorImages;

    // Геттеры и сеттеры
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthorFirstName() { return authorFirstName; }
    public void setAuthorFirstName(String authorFirstName) { this.authorFirstName = authorFirstName; }

    public String getAuthorLastName() { return authorLastName; }
    public void setAuthorLastName(String authorLastName) { this.authorLastName = authorLastName; }

    public String getAuthorMiddleName() { return authorMiddleName; }
    public void setAuthorMiddleName(String authorMiddleName) { this.authorMiddleName = authorMiddleName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public List<String> getInteriorImages() { return interiorImages; }
    public void setInteriorImages(List<String> interiorImages) { this.interiorImages = interiorImages; }
}
