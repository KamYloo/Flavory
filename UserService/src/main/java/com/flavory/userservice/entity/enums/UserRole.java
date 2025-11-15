package com.flavory.userservice.entity.enums;

public enum UserRole {
    CUSTOMER("Klient"),
    COOK("Kucharz"),
    ADMIN("Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}