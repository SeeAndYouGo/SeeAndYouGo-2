package com.SeeAndYouGo.SeeAndYouGo.aop;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}