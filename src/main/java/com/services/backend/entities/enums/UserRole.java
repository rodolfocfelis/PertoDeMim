package com.services.backend.entities.enums;

public enum UserRole {
    ADMIN("admin"),
    USER("user"),
    PROFESSIONAL("professional");

    private String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}