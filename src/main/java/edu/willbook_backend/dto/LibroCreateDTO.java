package edu.willbook_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LibroCreateDTO(
        @NotBlank(message = "Este campo es requerido")
        @Size(max = 150, message = "El título no puede superar los 150 caracteres")
        String titulo,

        @NotBlank(message = "Este campo es requerido")
        @Size(max = 150, message = "El autor no puede superar los 150 caracteres")
        String autor,

        @NotBlank(message = "Este campo es requerido")
        @Size(max = 20, message = "El ISBN no puede superar los 20 caracteres")
        String isbn,

        @Size(max = 100, message = "La editorial no puede superar los 100 caracteres")
        String editorial,

        @NotNull(message = "Este campo es requerido")
        Integer anioPublicacion,

        @NotBlank(message = "Este campo es requerido")
        @Size(max = 50, message = "La categoría no puede superar los 50 caracteres")
        String categoria,

        String portadaUrl,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion
) {}