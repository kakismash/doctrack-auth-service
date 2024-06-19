package com.kaki.doctrack.authservice.entity;

public enum ERole {

    SUPERADMIN,
    ADMIN,
    USER_READ_ONLY,
    USER_READ_WRITE;

    public static String nameFromId(int roleId) {
        return switch (roleId) {
            case 1 -> "SUPERADMIN";
            case 2 -> "ADMIN";
            case 3 -> "USER_READ_ONLY";
            case 4 -> "USER_READ_WRITE";
            default -> null;
        };
    }
    
}
