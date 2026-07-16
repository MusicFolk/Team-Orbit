package com.orbit.team.exception;

public class RsvpConflictException extends RuntimeException {
    public RsvpConflictException(String message) {
        super(message);
    }

    public RsvpConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}