package com.example.serverpublishingapp.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
    AUTHOR("author"),
    ADMIN("admin"),
    REVIEWER("reviewer");

    private final String dbValue;

    Role(String dbValue) {
        this.dbValue = dbValue;
    }
    
    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    @Override
    public String toString() {
        return dbValue;
    }

    @JsonCreator
    public static Role fromString(String value) {
        if (value == null) return null;
        for (Role r : Role.values()) {
            if (r.dbValue.equalsIgnoreCase(value) || r.name().equalsIgnoreCase(value)) return r;
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }

    public String toAuthority() {
        return "ROLE_" + this.name();
    }
}
