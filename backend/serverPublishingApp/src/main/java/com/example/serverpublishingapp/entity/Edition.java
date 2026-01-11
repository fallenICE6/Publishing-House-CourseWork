package com.example.serverpublishingapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "editions")
public class Edition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "author_first_name", nullable = false, length = 50)
    private String authorFirstName;

    @Column(name = "author_last_name", nullable = false, length = 50)
    private String authorLastName;

    @Column(name = "author_middle_name", length = 50)
    private String authorMiddleName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image", length = 255)
    private String coverImage;


    @ManyToMany
    @JoinTable(
            name = "edition_genres",
            joinColumns = @JoinColumn(name = "edition_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<Genre> genres = new ArrayList<>();

    @OneToMany(mappedBy = "edition", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<EditionImage> interiorImages = new ArrayList<>();


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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }


    public List<Genre> getGenres() { return genres; }
    public void setGenres(List<Genre> genres) { this.genres = genres; }

    public List<EditionImage> getInteriorImages() { return interiorImages; }
    public void setInteriorImages(List<EditionImage> interiorImages) { this.interiorImages = interiorImages; }

    public String getFullAuthorName() {
        List<String> parts = new ArrayList<>();
        if (authorFirstName != null) parts.add(authorFirstName);
        if (authorMiddleName != null && !authorMiddleName.trim().isEmpty()) parts.add(authorMiddleName);
        if (authorLastName != null) parts.add(authorLastName);
        return String.join(" ", parts);
    }
}