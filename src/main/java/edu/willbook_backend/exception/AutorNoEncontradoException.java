package edu.willbook_backend.exception;

public class AutorNoEncontradoException extends RuntimeException {
    public AutorNoEncontradoException(String message) {
        super(message);
    }
    public AutorNoEncontradoException(Long id) {
        super("Autor no encontrado con ID: " + id);
    }
}
