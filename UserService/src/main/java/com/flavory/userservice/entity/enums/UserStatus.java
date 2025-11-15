package com.flavory.userservice.entity.enums;

public enum UserStatus {
    ACTIVE("Aktywny"),
    INACTIVE("Nieaktywny"),
    SUSPENDED("Zawieszony"),
    PENDING_VERIFICATION("Oczekuje weryfikacji");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}