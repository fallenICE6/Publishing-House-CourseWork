package com.example.serverpublishingapp.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role attribute) {
        if (attribute == null) return null;
        return attribute.getDbValue();
    }

    @Override
    public Role convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;
        return Role.fromString(dbValue);
    }
}
