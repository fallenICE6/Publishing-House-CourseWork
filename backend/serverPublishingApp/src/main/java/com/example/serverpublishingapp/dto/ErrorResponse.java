package com.example.serverpublishingapp.dto;

public class ErrorResponse {
    private String code;

    public ErrorResponse(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
