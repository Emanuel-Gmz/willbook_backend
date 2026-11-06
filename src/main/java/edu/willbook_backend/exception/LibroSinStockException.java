package edu.willbook_backend.exception;

public class LibroSinStockException extends RuntimeException {
    public LibroSinStockException(String message) {
        super(message);
    }
}
