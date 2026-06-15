package com.example.serverpublishingapp.dto;

import jakarta.validation.constraints.NotBlank;

public class AddCommentRequest {
    @NotBlank(message = "Комментарий не может быть пустым")
    private String comment;

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}