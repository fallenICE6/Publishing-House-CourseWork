
package com.example.serverpublishingapp.dto;

public record UpdateUserRequest(
        String firstName,
        String lastName,
        String middleName,
        String email,
        String phone
) {}
