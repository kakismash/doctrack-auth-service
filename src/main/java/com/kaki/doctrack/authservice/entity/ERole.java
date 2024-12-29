package com.kaki.doctrack.authservice.entity;

public enum ERole {

    SUPER_ADMIN,
    ADMIN,
    ORGANIZATION_ADMIN,
    ORGANIZATION_WORKER;

    public static String nameFromId(int roleId) {
        return switch (roleId) {
            case 1 -> "SUPER_ADMIN";
            case 2 -> "ADMIN";
            case 3 -> "ORGANIZATION_ADMIN";
            case 4 -> "ORGANIZATION_WORKER";
            default -> null;
        };
    }


    public static boolean checkHierarchy(ERole role, ERole targetRole) {
        return switch (role) {
            case SUPER_ADMIN -> true;
            case ADMIN -> targetRole != SUPER_ADMIN;
            case ORGANIZATION_ADMIN -> targetRole != SUPER_ADMIN && targetRole != ADMIN;
//            case ORGANIZATION_WORKER -> targetRole == ORGANIZATION_WORKER;
            default -> false;
        };
    }
}
