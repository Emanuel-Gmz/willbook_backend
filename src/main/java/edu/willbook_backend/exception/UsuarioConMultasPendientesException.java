package edu.willbook_backend.exception;

public class UsuarioConMultasPendientesException extends RuntimeException {
    public UsuarioConMultasPendientesException(String message) {
        super(message);
    }
}
