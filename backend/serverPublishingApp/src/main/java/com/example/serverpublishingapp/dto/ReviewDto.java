package com.example.serverpublishingapp.dto;

public class ReviewDto {

    private String text;
    private String status;

    public ReviewDto(String text, String status) {
        this.text = text;
        this.status = status;
    }

    public String getText() {
        return text;
    }

    public String getStatus() {
        return status;
    }
}
