package com.example.serverpublishingapp.dto;

public class UserResponse {

    private Long id;
    private String username;
    private String phone;
    private String email;
    private String firstName;
    private String lastName;
    private String middleName;
    private String role;

    public UserResponse() {
    }

    public UserResponse(Long id, String username, String phone, String email,
                        String firstName, String lastName, String middleName, String role) {
        this.id = id;
        this.username = username;
        this.phone = phone;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.role = role;
    }

    // --- Геттеры и сеттеры ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
