package com.example.serverpublishingapp.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class EditionRequest {

    @NotBlank(message = "Название обязательно")
    @Size(max = 200, message = "Название не должно превышать 200 символов")
    private String title;

    @NotBlank(message = "Имя автора обязательно")
    private String authorFirstName;

    @NotBlank(message = "Фамилия автора обязательна")
    private String authorLastName;

    private String authorMiddleName;

    @Size(max = 5000, message = "Описание слишком длинное")
    private String description;

    private String coverImage;

    @NotEmpty(message = "Нужно указать хотя бы один жанр")
    private List<String> genres;

    private List<String> interiorImages;

    // ===== getters / setters =====

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
