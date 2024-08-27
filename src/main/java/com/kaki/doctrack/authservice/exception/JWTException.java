package com.kaki.doctrack.authservice.exception;

import lombok.Getter;

@Getter
public class JWTException extends RuntimeException {
    private final String errorCode;
    private final String errorMessage;

    public JWTException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
