package com.example.serverpublishingapp.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "edition_images")
public class EditionImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "edition_id", nullable = false)
    private Edition edition;

    @Column(name = "image_name", nullable = false, length = 255)
    private String imageName;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Edition getEdition() { return edition; }
    public void setEdition(Edition edition) { this.edition = edition; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}