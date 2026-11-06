package edu.willbook_backend.exception;

public class PasswordInvalidaException extends RuntimeException {
    public PasswordInvalidaException(String message) {
        super(message);
    }
}
