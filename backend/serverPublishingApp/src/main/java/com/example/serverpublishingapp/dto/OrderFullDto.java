package com.example.serverpublishingapp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderFullDto {

    private Long id;


    private String fullName;
    private String email;
    private String phone;


    private String serviceTitle;


    private Integer pages;
    private Integer quantity;


    private List<OrderMaterialDto> materials;


    private List<OrderFileDto> files;


    private ReviewDto review;

    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime createdAt;

    private List<OrderCommentDto> comments;
    public OrderFullDto(
            Long id,
            String fullName,
            String email,
            String phone,
            String serviceTitle,
            Integer pages,
            Integer quantity,
            List<OrderMaterialDto> materials,
            List<OrderFileDto> files,
            ReviewDto review,
            BigDecimal totalPrice,
            String status,
            LocalDateTime createdAt,
            List<OrderCommentDto> comments
    ) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.serviceTitle = serviceTitle;
        this.pages = pages;
        this.quantity = quantity;
        this.materials = materials;
        this.files = files;
        this.review = review;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.comments = comments;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getServiceTitle() { return serviceTitle; }
    public Integer getPages() { return pages; }
    public Integer getQuantity() { return quantity; }
    public List<OrderMaterialDto> getMaterials() { return materials; }
    public List<OrderFileDto> getFiles() { return files; }
    public ReviewDto getReview() { return review; }
    public void setReview(ReviewDto review) { this.review = review; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderCommentDto> getComments() { return comments; }
    public void setComments(List<OrderCommentDto> comments) { this.comments = comments; }
}

