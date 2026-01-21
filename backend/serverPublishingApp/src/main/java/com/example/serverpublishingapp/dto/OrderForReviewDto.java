package com.example.serverpublishingapp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderForReviewDto {
    private Long id;
    private String customerName;
    private String serviceTitle;
    private Integer pages;
    private Integer quantity;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private List<OrderFileDto> files;
    private boolean hasReview;
    private String status;

    public OrderForReviewDto() {}

    public OrderForReviewDto(Long id, String customerName, String serviceTitle,
                             Integer pages, Integer quantity, BigDecimal totalPrice,
                             LocalDateTime createdAt, List<OrderFileDto> files,
                             boolean hasReview, String status) {
        this.id = id;
        this.customerName = customerName;
        this.serviceTitle = serviceTitle;
        this.pages = pages;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.files = files;
        this.hasReview = hasReview;
        this.status = status;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getServiceTitle() { return serviceTitle; }
    public void setServiceTitle(String serviceTitle) { this.serviceTitle = serviceTitle; }

    public Integer getPages() { return pages; }
    public void setPages(Integer pages) { this.pages = pages; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<OrderFileDto> getFiles() { return files; }
    public void setFiles(List<OrderFileDto> files) { this.files = files; }

    public boolean isHasReview() { return hasReview; }
    public void setHasReview(boolean hasReview) { this.hasReview = hasReview; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}