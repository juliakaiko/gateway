package com.mymicroservice.gateway.exception;

public class AuthServiceException extends RuntimeException{

    public AuthServiceException(String message) {
        super(message);
    }
}
