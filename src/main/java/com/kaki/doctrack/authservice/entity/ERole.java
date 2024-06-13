package com.kaki.doctrack.authservice.entity;

public enum ERole {

    SUPERADMIN,
    ADMIN,
    USER_READ_ONLY,
    USER_READ_WRITE;
    
    Enum<ERole> fromRoleName(String roleName) {
        return switch (roleName) {
            case "SUPERADMIN" -> ERole.SUPERADMIN;
            case "ADMIN" -> ERole.ADMIN;
            case "USER_READ_ONLY" -> ERole.USER_READ_ONLY;
            case "USER_READ_WRITE" -> ERole.USER_READ_WRITE;
            default -> null;
        };
    }
    
}
