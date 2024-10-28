package com.kaki.doctrack.authservice.entity;

public enum ERole {

    SUPER_ADMIN,
    ADMIN,
    ORGANIZATION_ADMIN,
    USER_READ_ONLY,
    USER_READ_WRITE;

    public static String nameFromId(int roleId) {
        return switch (roleId) {
            case 1 -> "SUPER_ADMIN";
            case 2 -> "ADMIN";
            case 3 -> "ORGANIZATION_ADMIN";
            case 4 -> "USER_READ_ONLY";
            case 5 -> "USER_READ_WRITE";
            default -> null;
        };
    }
    
}
