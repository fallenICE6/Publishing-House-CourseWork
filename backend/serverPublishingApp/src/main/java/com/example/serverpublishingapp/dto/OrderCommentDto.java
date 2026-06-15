package com.example.serverpublishingapp.dto;

import java.time.LocalDateTime;

public class OrderCommentDto {
    private Long id;
    private Long userId;
    private String userName;
    private String userRole;
    private String comment;
    private Boolean isSystem;
    private LocalDateTime createdAt;

    public OrderCommentDto() {}

    public OrderCommentDto(Long id, Long userId, String userName, String userRole,
                           String comment, Boolean isSystem, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
        this.comment = comment;
        this.isSystem = isSystem;
        this.createdAt = createdAt;
    }

    // Геттеры
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserRole() { return userRole; }
    public String getComment() { return comment; }
    public Boolean getIsSystem() { return isSystem; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}