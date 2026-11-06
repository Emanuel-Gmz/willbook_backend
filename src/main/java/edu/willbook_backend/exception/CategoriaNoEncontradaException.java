package edu.willbook_backend.exception;

public class CategoriaNoEncontradaException extends RuntimeException {
    public CategoriaNoEncontradaException(String message) {
        super(message);
    }
    public CategoriaNoEncontradaException(Long id) {
        super("Categoría no encontrada con ID: " + id);
    }
}
