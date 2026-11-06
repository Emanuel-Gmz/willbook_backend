package edu.willbook_backend.exception;

import jakarta.validation.constraints.NotNull;

public class UsuarioNoEncontradoException extends RuntimeException {
    public UsuarioNoEncontradoException(String message) {
        super(message);
    }

    public UsuarioNoEncontradoException(@NotNull(message = "El ID del usuario es obligatorio") Long aLong) {
    }
}
