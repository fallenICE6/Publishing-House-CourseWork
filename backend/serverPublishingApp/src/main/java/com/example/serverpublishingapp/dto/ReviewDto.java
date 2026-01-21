package com.example.serverpublishingapp.dto;

import java.time.LocalDateTime;

public class ReviewDto {
    private Long id;
    private Long orderId;
    private Long reviewerId;
    private String reviewerName;
    private String comment;
    private String status;
    private String orderStatusAfterReview;
    private LocalDateTime createdAt;

    public ReviewDto() {}

    public ReviewDto(Long id, Long orderId, Long reviewerId, String reviewerName,
                     String comment, String status, String orderStatusAfterReview,
                     LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.reviewerId = reviewerId;
        this.reviewerName = reviewerName;
        this.comment = comment;
        this.status = status;
        this.orderStatusAfterReview = orderStatusAfterReview;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOrderStatusAfterReview() { return orderStatusAfterReview; }
    public void setOrderStatusAfterReview(String orderStatusAfterReview) {
        this.orderStatusAfterReview = orderStatusAfterReview;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}