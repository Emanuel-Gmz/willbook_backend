package edu.willbook_backend.dto;

import jakarta.validation.constraints.Size;

public record LibroUpdateDTO(
        @Size(max = 150, message = "El título no puede superar los 150 caracteres")
        String titulo,

        Long autorId,

        @Size(max = 20, message = "El ISBN no puede superar los 20 caracteres")
        String isbn,

        @Size(max = 100, message = "La editorial no puede superar los 100 caracteres")
        String editorial,

        Integer anioPublicacion,

        Long categoriaId,

        String portadaUrl,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion,

        Boolean disponible
) {}